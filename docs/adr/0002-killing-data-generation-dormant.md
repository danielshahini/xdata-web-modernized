# ADR 0002 — Killing-data generation is dormant; legacy datagen config is a stub

- **Status:** Accepted (2026-06-17)
- **Relates to:** "what's missing" item #5 (`DatasetGenerationService.extractInsertsFromModel` returns empty)

## Context

XData's signature capability is generating "killing data" (targeted test data, via an
SMT solver) to grade SQL queries. In this codebase `extractInsertsFromModel(...)` is a
stub that always returns an empty list, so the **test-data equivalence rung never
produces data**. A spike was run to fix it properly. Findings:

1. **Native Z3 was not even loaded.** `SmtSolverService` used `System.loadLibrary("z3")`,
   which failed → the app silently fell back to the z3 **CLI**. *(Fixed — see Decision.)*

2. **The legacy datagen pipeline NPEs before it ever solves.**
   `com.xdata.legacy.database.Configuration.getProperty(...)` is a hand-written stub that
   only knows 8 keys (db name/user/ip/port/homeDir) and returns **null for every
   datagen key** (`tempDatabaseType`, `cntFlag`, `isEnumInt`, `smtsolver`, `regressDS0`,
   `existsUnrollFlag`, `tempJoins`, …). The pipeline throws immediately at
   `util.TableMap.<init>` (`Configuration.getProperty("tempDatabaseType").equalsIgnoreCase(...)`
   → NPE). `getLegacyTableMap` → `GenerateCVC1` → constraint generation all sit behind this.

3. **Consequence:** both the **test-data rung AND the SMT-equivalence rung** of the
   equivalence chain are effectively dormant in the modernized app. Grading currently runs
   on text-equality + assigned-DB compare + partial marking.

4. **The model format would also need bespoke handling.** z3 4.x emits tuple models as
   nested array/`store` (or function `FuncInterp`) S-expressions; the legacy string parser
   `requiredSqlOutput` expects flat `(<T>_TupleType v..)` lines and does not match. The
   array/function-aware extractor `cutRequiredOutputForSMTWithAPI` needs the Z3 `Model`
   object and is entangled with `ConstraintGenerator.ctx` + `Configuration` + file I/O +
   a `cntFlag` gate.

Crucially, **post-ADR-implied K1 the empty stub is safe**: an empty dataset yields
`INCONCLUSIVE`, which degrades to SMT / assigned-DB / partial marking. It is a dormant
capability, not a correctness bug. Implementing it wrong would *regress* to wrong grades.

## Decision

**Do not blind-implement the extraction.** Restoring killing-data is a reconstruction
project, not a feature wire-up, and must be done with a verification oracle. The required
steps, in order:

1. Populate the legacy datagen `Configuration` with correct values (requires XData-internal
   knowledge of `tempDatabaseType`, `cntFlag`, `isEnumInt`, `smtsolver`, etc.) and remove the
   `getProperty` stub's null-for-everything behaviour.
2. Confirm `GenerateCVC1` produces a parseable, SAT SMT model on modern z3 for a real
   schema+query (it is 2011-era code).
3. Port the `Model`→INSERT extraction (core of `cutRequiredOutputForSMTWithAPI` +
   `requiredSqlOutputNew`), untangled from `Configuration`/file I/O, reading the model within
   the solving `Context`.
4. Add a golden end-to-end test (known schema + query → INSERTs that run and exercise the
   predicate) before shipping.

## What was done (Path A foundation)

- Replaced the vendored `com.microsoft:z3:1.0` (z3 ~4.3.2, no bundled natives) with
  **`tools.aqua:z3-turnkey:4.12.2.1`**, which bundles + self-loads native libraries.
- `SmtSolverService` now detects Z3 by creating a `Context` (turnkey self-load) instead of
  `System.loadLibrary` → **native Z3 is enabled** (verified locally and in the Linux
  container; `Z3NativeSmokeTest` guards it). This is the correct foundation for step 3 above,
  though it does not change runtime grading today (the pipeline is still dormant per #2).

## Reconstruction progress (2026-06-17, step 1 + front-end done)

A reconstruction pass got the pipeline **from "NPEs on the first config key" all the way
through query parsing**. Steps 1 and 2's front-end are now done and guarded by a golden
test (`DatagenPipelineGoldenTest`, run against a real PostgreSQL).

Bugs fixed (all verified; full backend suite still green, 82 tests):

1. **`legacy.database.Configuration`** now falls back to the bundled `util/XData.properties`
   for the 12 datagen keys (runtime-set fields keep priority). Fixes the first NPE
   (`tempDatabaseType` → null) at `TableMap.<init>`.
2. **`legacy.util.Configuration`** (a *second*, separate stub used by `GenerateCVC1`) loaded
   `XData.properties` relative to its own package, where the file does not live → threw NPE
   for every key. Now loads the resource by absolute classpath `/util/XData.properties`.
3. **`TableMap.createTableMap`** built the legacy `Table` with the raw DB identifier.
   PostgreSQL folds unquoted identifiers to **lowercase**, but the parser resolves tables via
   `getFromTables().get(name.toUpperCase())` while `addFromTable` keys by `table.getTableName()`
   → every column lookup missed and **every query failed to parse**. Now the `Table` is built
   with the upper-cased name. (Original XData assumed upper-case identifiers, so this never
   surfaced there.)
4. **`WhereClauseVectorJSQL`**: the `> ALL(...)`-subquery branch of every comparison operator
   guarded on `instanceof Expression`, which is **always true** in JSQLParser (everything is an
   `Expression`) — a modernization artefact (JSQLParser 5 merged `AllComparisonExpression` into
   `AnyComparisonExpression`). So plain `col > 20` entered the subquery branch and NPE'd on
   `getSubQueryConds().clear()`. Guard changed to `instanceof AnyComparisonExpression`.
5. **`WhereClauseVectorJSQL.getAggregationDataStructures`** dereferenced `getGroupBy()` without
   a null check; modern JSQLParser returns `null` for a missing GROUP BY. Added the guard.
6. The legacy parser also requires a **non-null `AppTest_Parameters`** and **table-qualified
   column references** (`students.id`, not `id`) — now passed by `DatasetGenerationService`
   and asserted by the golden test.

**Net effect:** `schema metadata → TableMap → QueryParser` works end-to-end on PostgreSQL
(was 100 % broken). Runtime grading is unchanged; the Dataset Playground still returns an
empty result with the honest message.

## Remaining frontier (step 3, the engine) — now runs end-to-end, two correctness bugs left

The actual constraint generation + solve + extraction is **not** the small
`inititalizeSQDataset()+getCVCStr()` sequence that was stubbed in — those return `null`. The
real driver is:

```
DataGenController → GenerateCVC1.generateDatasetsToKillMutations()
  → GenConstraints.generateDatasetForNonEmptyDataset (case 1: non-empty dataset)
    → GenerateDataForOriginalQuery.generateDataForOriginalQuery
      → GenerateCommonConstraintsForQuery.generateDataSetForConstraints   ← builds SMT, solves, writes SQL
        → PopulateTestData.killedMutantsForSMT → cutRequiredOutputForSMTWithAPI  ← native-API extraction
```

A reconstruction pass drove this whole flow against a real PostgreSQL (`cvc` given a JDBC
connection, `homeDir/temp_smt/` provisioned, `QueryStructure.buildQueryStructureJSQL →
initializeQueryDetailsQStructure → populateData → initializeOtherDetails →
generateDatasetForNonEmptyDataset`). **It now runs without crashing**: GenerateCVC1 emits a
valid SMT, native Z3 parses + solves it, and the extractor runs. Two correctness bugs remain,
both root-caused (resume harness: `DatagenPipelineGoldenTest.producesNonEmptyDataset`):

1. **WHERE selection mis-mapped under `outerSQ=true` (the default) → UNSAT. [FIXED]** The query
   was wrapped as a subquery (`JSQ0`) and the predicate `students.age > 20` was emitted on the
   subquery **count** column (`JSQ0__XDATA_CNT`) instead of `JSQ0_STUDENTS__AGE1` → with the
   count bounded to `<= 1`, `count > 20 ∧ count <= 1` → UNSAT. Root cause: the *same*
   case-mismatch the parser had — `GenerateJoinPredicateConstraints` compared
   `table.getTableName().equals(left)` where `left` is lower-cased, but the TableMap fix made
   `getTableName()` upper-case, so the table never matched, `l_flag` stayed `-1`, and the column
   index ran past the real columns onto the appended `XDATA_CNT`. Fixed by making the six
   table-name comparisons in `GenerateJoinPredicateConstraints` case-insensitive
   (`equalsIgnoreCase`). The engine now emits a **satisfiable** SMT whose model is correct data
   (`(STUDENTS_TupleType 1 21 1)` → id=1, age=21). Guarded by
   `DatagenPipelineGoldenTest.engineGeneratesSatisfiableDatasetConstraints`.
   (`outerSQ=false` would avoid the wrapping but emits *no* selection constraint → trivially SAT
   with `age=0` → data that does not satisfy the query; not a valid shortcut, left as `outerSQ=true`.)

2. **Array-encoded model needs an array-aware extractor. [FIXED]** The SMT encodes a table as
   `O_T : (Array Int T_TupleType)`; the model comes back as e.g.
   `((as const ...) (STUDENTS_TupleType 0 0 1))`. The legacy extractor
   `cutRequiredOutputForSMTWithAPI` looked for `FuncDecl`s whose range *ends with*
   `_TupleType` and found none (the array constant's range is `(Array Int …_TupleType)`),
   so it wrote an empty `DS*.sql`. Added `PopulateTestData.generateInsertsFromArrayModel`:
   for each `O_T` it reads `model.eval(select(O_T, i))` for `i in 1..count`
   (`count` from `noOfOutputTuples`), decomposes the tuple-type accessors into column values,
   uses the trailing `XDATA_CNT` accessor as the row multiplicity, maps the -99999 NULL
   sentinel, and emits `INSERT`s. Wired into `cutRequiredOutputForSMTWithAPI` as the fallback
   when the legacy loop produces nothing and the model is SAT.

Both bugs are now fixed. The full pipeline — query → constraints → native Z3 → array-aware
extraction → runnable INSERTs — works end-to-end, proven by
`DatagenPipelineGoldenTest.producesNonEmptyInsertStatements` (e.g.
`SELECT id FROM students WHERE age > 20` → `insert into students values (null, 21)`).

## Playground wired [DONE]

`DatasetGenerationService` now drives the real engine for the playground
(`generateDatasetFromQuery(query, mutantQuery, schemaId, mutationTypes)` →
`generateDatasetViaEngine` → `runEngine`). Because the legacy `TableMap`/`GenerateCVC1` are
**PostgreSQL-native** (schema `public`, `pg_constraint`, …) and incompatible with the Derby
scratch DB, `runEngine` provisions an **isolated throwaway PostgreSQL database**: it
`CREATE DATABASE`s a uniquely-named temp DB, applies the uploaded DDL, sets a per-request
`temp_smt` working dir, drives `buildQueryStructureJSQL → … → generateDatasetForNonEmptyDataset`,
reads back the `DS*.sql` INSERTs, and in a `finally` drops the temp DB (`DROP DATABASE … WITH
(FORCE)`) and deletes the temp dir. The whole generation is serialized by a static
`ENGINE_LOCK` because the engine drives shared static state (`Configuration.homeDir`, the single
`ConstraintGenerator.ctx`). `TableMap.clearAllInstances()` is called per request to avoid the
static schema cache returning a stale map.

**Verified live** through the HTTP API: upload `students (id, age)`, then
`POST /playground/generate-killing-data` with `SELECT students.id FROM students WHERE students.age > 20`
→ `{success:true, inserts:["insert into students values (null, 21);"]}`. Temp DBs are cleaned up
(0 left after repeated calls).

**Targeted killing data (mutationTypes) wired.** `runEngine` now generates the base non-empty
dataset *plus* one targeted "killing" dataset per requested mutation type: each `mutationTypes`
entry (`SELECTION`, `EQUIVALENCE`, `NONEQUIJOIN`, `AGG`, `DISTINCT`, `EXTRAGROUPBY`, `HAVING`)
maps via `TagDatasets.MutationType`/`mutationTypeNumber.valueOf` to
`GenConstraints.generateConstraintsToKillMutations`. Each type runs in its own try/catch so an
unsupported/failing type is skipped, not fatal. Verified live: `SELECT students.id FROM students
WHERE students.age > 20` with `["SELECTION","AGG","HAVING","FOO"]` →
`insert into students values (null, 21)` (base), `(4, 20)` (boundary value that kills the
`age >= 20` mutant), `(4, -1)`; `FOO` skipped, AGG/HAVING produce nothing (no target). The full
suite is guarded by `DatagenPipelineGoldenTest.drivesFullMutationKillingSuite`.

**Scope:** only the *playground* path is wired. The grading path
(`generateDatasetFromQuery(query, schemaId)` / `generateKillingDataset`) is left unchanged on
the safe `INCONCLUSIVE` fallback — enabling killing-data in grading is a separate change that
must validate how the equivalence chain consumes the generated data. The playground query must
use **table-qualified columns** (legacy parser requirement). `mutantQuery` (distinguishing the
query from one specific arbitrary mutant) is a different comparison path and remains unwired.

## Revisit when

There is appetite to reconstruct the constraint-generation engine (step 3). The front-end
(steps 1–2) and native Z3 (foundation) are now in place; the safe `INCONCLUSIVE` fallback
still stands for grading.
