

# Context — Domain & Architecture Glossary

Shared vocabulary for xdata-web. Domain terms name the concepts the SQL-grading
system reasons about; architecture terms (module, interface, seam, adapter,
depth, leverage, locality) follow `.claude/skills/improve-codebase-architecture/LANGUAGE.md`.

## Domain

**Submission** — one student attempt at a Question: the stored student query plus
its grading result (verdict, marks, XP earned). Entity: `model/Submission`.

**Question** — a single SQL task within an Assignment: the instructor (pattern)
query, the schema, partial-mark weights, max marks. Entity: `model/Question`.

**Assignment** — a graded set of Questions for a Course, bound to a `DbConnection`
and a default schema. Entity: `model/Assignment`.

**Instructor query / student query** — the reference (pattern) solution and the
learner's attempt. Equivalence grading asks whether they are interchangeable.

**Equivalence verdict** — the answer to "is the student query equivalent to the
instructor query?". Three-state, NOT boolean:
- `EQUIVALENT` — proven the same.
- `NOT_EQUIVALENT` — proven different.
- `INCONCLUSIVE` — no method could decide (no test data generated, SMT
  unavailable/unknown, no assigned DB). Must NOT collapse to `NOT_EQUIVALENT` —
  doing so silently zeroes a correct answer when infrastructure is missing.

**Equivalence chain / rung** — the ordered fallback that produces the verdict:
text-equality → test-data-execution → SMT → assigned-DB. Each rung either decides
or abstains (`INCONCLUSIVE`); the first decisive rung wins. Order is behaviour:
cheap/pure first, the live assigned-DB compare last.

**Partial marking** — structural scoring of how close a non-equivalent query is to
the pattern (projections, predicates, joins, group-by, …), producing a `MarkInfo`
breakdown. Implemented in `partialmarking/core/PartialMarker`. Independent of the
equivalence verdict; always computed so a wrong answer still earns partial credit.

**Penalty** — fraction subtracted from raw marks for prior attempts/timing.
`SubmissionService.calculatePenalty`.

**XP** — gamification points awarded on improvement over a student's previous best
for a Question.

**Playground dry-run** — evaluating a pattern/answer pair with custom weights and
NO persistence or side effects, for the instructor playground. Shares the marking
path with real grading.

## Architecture

**SubmissionEvaluation** — the deep module that owns the whole evaluation
orchestration for one Submission. External interface (thin `@Service` shell):
`grade(id)` (async, full side effects), `gradeNow(id)` (sync, returns outcome),
`dryRun(request)` (no persistence). Replaces the god-method
`GradingService.evaluateSubmission`.
- Shell holds `@Transactional` / `@CacheEvict` / `@Async`, loads entities, maps to
  pure records, invokes the core, fires side-effect ports.
- **Decision core** — pure function `evaluate(EvaluationRequest) → GradingOutcome`;
  no JPA, no I/O. Sequences the equivalence rungs, runs partial marking, then the
  `VerdictScorer`. Unit-testable with zero infrastructure.

**EquivalenceStageCheck** — internal seam: one rung of the equivalence chain.
`check(QueryPair, SchemaRef) → EquivalenceResult`. Invariant: never throws; an
internal failure becomes `INCONCLUSIVE`. Real seams (prod + in-memory adapters):
SMT, test-data-execution, assigned-DB. Text-equality is inlined (one adapter, not
a real seam).

**VerdictScorer** — pure component combining verdict + `MarkInfo` + penalty into the
final marks, clamped to [0,1].

**Ports** (impure boundaries, each with a prod adapter and an in-memory test fake):
`PartialMarkingPort`, `GradeSink` (persist + `bestPriorMark`), `ResultNotifier`
(WebSocket), `LmsGradePublisher`. XP currently folded into `GradeSink` until a
second reason to split it appears.

**SubmissionAnalytics** (`com.xdata.service.core`) — the single home for read-side
aggregation over submissions: `dashboard(user, courseIds)`, `assignmentStats(id)`,
`resultsCsv(id)`, `leaderboard(courseId)`. Extracted from the controllers (which
now delegate) and from `SubmissionService.getLeaderboard`, so aggregation is
testable without the HTTP stack and "best mark per question" is defined once.

**QueryRunner** (`com.xdata.db`) — the deep module for running a query against a
database and extracting its rows. `run(Connection, sql, timeout, maxRows) →
ResultRows`. The single home of result-set extraction (was duplicated in
TestExecutionService, AssignedDbStage). Sandbox validation stays with the caller.

**ResultRows** — the rows of a query, with the one canonical comparison
`matches(other)`: order-insensitive multiset equality. Replaces the two divergent
comparisons (true multiset vs sort-by-toString) that could disagree.

**CourseAccess / CourseAccessGuard** (`com.xdata.security`) — the single seam for
enforcing authorization in controllers: `requireAdmin()`,
`requireInstructorOrAdmin()`, `requireCourseAccess(courseId)`. Each throws
`AccessDeniedException` on denial → one consistent 403 JSON via
`GlobalExceptionHandler`. Replaces the scattered `if (!check) return
ResponseEntity.status(403)...` idioms (three variants, two error shapes). The
policy primitives stay in `AccessControlService`; the guard concentrates the
check + throw + error shape. Pure role gates keep using `@PreAuthorize`.

**ScratchDatabase / ScratchDatabaseFactory** — an ephemeral in-memory Derby
database loaded with a schema (and optional test data), `AutoCloseable` (drops on
close). The single owner of the Derby-memory lifecycle (was duplicated in
TestExecutionService and MetadataService). Connection acquisition to a real
assigned database stays in `DatabaseService` (driver loading); AssignedDbStage now
routes through it instead of opening `DriverManager` directly.

**Dual TableMap / query model** — `util/TableMap` + `legacy/parsing/Query` (SMT
data generation) and `partialmarking/core/TableMap` + `QueryStructure` (partial
marking) are intentionally separate, not duplication. See
[ADR 0001](docs/adr/0001-keep-dual-tablemap-and-query-representations.md) — do not
re-propose unifying them.

## Frontend (React/TS)

**createSqlCompletionProvider** (`src/utils/sqlCompletion.ts`) — the single Monaco
SQL auto-completion provider, built from schema metadata. `monaco` is passed in
(not imported) so it is a pure, unit-tested function. Replaces the ~40-line
provider that was copy-pasted into every SQL editor (SqlLab, StudentDashboard).

**useAsyncData** (`src/hooks/useAsyncData.ts`) — the single seam for data fetching:
`useAsyncData(fetcher, deps) → { data, loading, error, retry }`. Replaces the
hand-rolled `useState(loading/error/data) + useEffect + try/catch` blocks that were
copy-pasted (with inconsistent error handling — silent, console, toast) across
components. Unit-tested via `@testing-library` `renderHook`. Migrated: `Leaderboard`,
`AuditLogViewer`, `SchemaManager`, `DbConnectionManager`, `TestDataViewer`,
`UserManager`, `InstructorDashboard`, `AssignmentStats`, `DatasetPlayground`,
plus the `SqlLab`/`StudentDashboard` schema-metadata fetches (mutations reload via
`retry()`). Remaining inline `api.*` calls are mutations (actions, not loads) and
the WebSocket-driven dashboard/submission reloads — neither is a `useAsyncData`
fit.
