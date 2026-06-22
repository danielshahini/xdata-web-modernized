# ADR 0001 — Keep the dual TableMap / query representations

- **Status:** Accepted (2026-06-17)
- **Deciders:** maintainers
- **Context skill:** surfaced as "Candidate 6" by `improve-codebase-architecture`

## Context

The codebase holds two parallel schema-metadata and query representations:

| | Legacy (`util/TableMap`, `legacy/parsing/Query`) | New (`partialmarking/core/TableMap`, `QueryStructure`) |
|---|---|---|
| Purpose | SMT-based test-data generation | Structural partial marking |
| Built by | `MetadataService.getLegacyTableMap` — **JDBC** from a live DB (`TableMap.getInstances(Connection)`) | `MetadataService.getNewTableMap` — **DDL string** (`SqlSchemaParser` + `TableMapBuilder`) |
| Holds | FK graph + topological sort, check constraints, numeric bounds, precision/scale, subquery tables | table + column names only |
| Query model | typed `Column`/`Node`, join graph (`JoinClauseInfo`), WHERE/JOIN separated, FK nodes — custom parser | `List<String>` + JSQLParser `Expression`, no WHERE/JOIN split, no FKs |
| State | mutable (`setSQTables`, static cache) | immutable DTO holder |
| Public API | ~10 methods | 3 methods |
| Consumers | ~41 `legacy/` files, rooted in `QueryParser` → `GenerateCVC1` (3000+ lines) | 2 files (`MetadataService`, `XDataPartialMarkingAdapter`) |

An architecture review repeatedly flags this as duplication and proposes unifying
the two into one TableMap / one query model.

## Decision

**Keep the two representations separate. Do not unify them, and do not introduce a
shared `TableSchema` interface.**

## Rationale

1. **Orthogonal purpose, not duplicated concept.** The legacy world is a
   constraint-solver backend that needs the FK graph, check constraints and numeric
   bounds to drive Z3. The new world is a query-marking frontend that needs only a
   name catalog for column qualification. A unified model would force ~60% unused
   metadata onto the marking path and pollute its clean immutable DTO design with
   JDBC/`setSQTables` state.

2. **No shared caller.** `MetadataService` vends one *or* the other depending on
   context; nothing consumes "either TableMap" polymorphically. By the seam
   principle (*one adapter = hypothetical seam; two = real*), even a common
   read-only `TableSchema { getTables(); getTable(name) }` interface would be a
   hypothetical seam — abstraction on spec, no leverage.

3. **Incompatible construction & lifecycle.** Legacy is JDBC-driven, long-lived and
   cached; new is DDL-driven, per-request and ephemeral. There is no single
   construction path that serves both.

4. **Risk vs. reward.** Unification would reach into ~41 tightly-coupled `legacy/`
   classes (custom parser, constraint generation, mutation killing) that currently
   have **no test net**. Very high blast radius for no behavioural gain.

## Consequences

- The apparent duplication stays. That is accepted: the two models are not the same
  thing wearing two hats.
- Future architecture reviews should treat this split as intentional and **not**
  re-propose unification unless the preconditions below change.
- `MetadataService` remains the boundary that builds the correct representation per
  consumer.

## Revisit if

- The `legacy/` test-data-generation core gains a real test net (then a careful,
  net-backed consolidation could be reconsidered), **and**
- a genuine shared consumer emerges that needs to treat both uniformly, **or**
- the legacy SMT/datagen path is retired entirely (then the new model may simply
  absorb whatever little it still needs).
