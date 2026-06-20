# Änderungsprotokoll — Architektur-, Test- & Betriebs-Überarbeitung (2026-06-17)

Diese Datei dokumentiert die in dieser Sitzung durchgeführten Änderungen: jeweils
**Vorher → Änderung → Warum**. Ergänzende Dokumente:
- [`CONTEXT.md`](../CONTEXT.md) — Domänen-/Architektur-Glossar (die neuen Module/Seams).
- [`docs/adr/0001-...`](adr/0001-keep-dual-tablemap-and-query-representations.md) — Entscheidung: dualen TableMap/Query-Split behalten.
- [`docs/adr/0002-...`](adr/0002-killing-data-generation-dormant.md) — Entscheidung: Killing-Data-Generierung ruht (Rekonstruktionsprojekt).

> **Build-Hinweis:** Das Backend muss mit **JDK 21** gebaut werden (Lombok 1.18.32 ist
> mit JDK 26 inkompatibel). Tests laufen offline (kein Postgres nötig).

---

## 1. Backend-Architektur: God-Methods in tiefe Module zerlegt

### 1.1 SubmissionEvaluation-Modul (Kandidat 1)
- **Vorher:** `GradingService.evaluateSubmission` war eine ~70-zeilige God-Method, die
  fünf Dinge inline orchestrierte: Text-Gleichheit, Testdaten-Vergleich, SMT, Ziel-DB-
  Vergleich und Partial Marking — JDBC und Z3 direkt eingebettet, kein Seam, nicht testbar.
  Ein nicht-entscheidbarer Fall („unschlüssig") kollabierte still zu `false` und konnte eine
  korrekte Lösung mit 0 bewerten.
- **Änderung:** Neues Modul `com.xdata.eval` — dünne Spring-Schale `SubmissionEvaluationService`
  (`gradeNow`/`dryRun`) über einem **reinen** `EvaluationEngine` (kein I/O). Die Äquivalenz-Kette
  ist eine geordnete Liste von `EquivalenceStageCheck`-Rungs (Text → TestData → SMT → AssignedDb),
  erster definitiver Treffer gewinnt; Drei-Zustand-`EquivalenceResult` (EQUIVALENT/NOT_EQUIVALENT/
  **INCONCLUSIVE**). Seiteneffekte hinter Ports (`GradeSink`, `ResultNotifier`, `PartialMarkingPort`).
  `GradingService` gelöscht; `EvaluationService`/`EvaluationController` umverdrahtet.
- **Warum:** Tiefe statt Breite — kleine Schnittstelle, testbarer Kern; `INCONCLUSIVE` ist jetzt
  ein echter Zustand (behebt den stillen Falsch-Bewertungs-Bug). Siehe `CONTEXT.md → SubmissionEvaluation`.

### 1.2 Query-Ausführungs-Seam (Kandidat 2)
- **Vorher:** Roher `DriverManager`/Derby-Code + Row-Extraktion an 3 Stellen dupliziert; Ergebnis-
  Vergleich an 2 Stellen mit **unterschiedlicher Semantik** (echtes Multiset vs. sort-by-`toString`)
  → konnten uneinig sein; `DatabaseService` existierte, wurde aber umgangen.
- **Änderung:** Neues Modul `com.xdata.db` — `QueryRunner` (Ausführung + Row-Extraktion an *einer*
  Stelle), `ResultRows.matches` (*eine* kanonische ordnungs-unabhängige Multiset-Semantik),
  `ScratchDatabase`/`ScratchDatabaseFactory` (Derby-in-memory-Lifecycle an *einer* Stelle).
  `TestExecutionService`, `AssignedDbStage`, `MetadataService` darauf umgestellt; `AssignedDbStage`
  routet jetzt durch `DatabaseService`.
- **Warum:** Duplikation + die latente Vergleichs-Inkonsistenz beseitigt; `DatabaseService` nicht mehr umgangen.

### 1.3 Submission-Analytics-Modul (Kandidat 3)
- **Vorher:** CSV-Export, Dashboard-Aggregation und Statistiken steckten inline in den Controllern,
  die teils direkt Repositories ansprachen; „best mark per question" 3× dupliziert.
- **Änderung:** `SubmissionAnalytics` (`dashboard`/`assignmentStats`/`resultsCsv`) — Aggregation aus
  `AssignmentController`/`StudentController` herausgezogen; direkte Repo-Felder aus den Controllern entfernt.
- **Warum:** Aggregation ohne HTTP-Stack testbar, „best per question" einmal definiert.

### 1.4 Partial-Marking God-Method zerlegt (Kandidat 5)
- **Vorher:** `PartialMarker.getMarks` war eine statische ~100-Zeilen-Methode mit 10+ Dimensionen inline;
  **kein** echter Test (`PartialMarkerTest` war substanzlos).
- **Änderung:** Zuerst **Charakterisierungs-Tests** (`PartialMarkerCharacterizationTest`) geschrieben, dann
  in benannte Schritte zerlegt: `compareDimensions`, `havingMark`, `distinctMark`, `aggregateScore`.
  `getMarks` bleibt der Seam; Verhalten byte-identisch.
- **Warum:** Lesbarkeit/Locality + endlich ein echtes Test-Netz. (Die frühere Methoden-Duplikation
  `calculatePartialMarksForPlayground`/`calculateXDataPartialMarks` war bereits durch K1 beseitigt.)

---

## 2. Autorisierung vereinheitlicht (Kandidat 4)

- **Vorher:** ~50 Inline-Zugriffsprüfungen über 7 Controller in **3 Idiomen**
  (`isInstructor()&&isAdmin()`-Gates, `canAccessCourse()`-Gates, in `CourseController` von Hand
  via `getUserCourseIds().contains(...)` nachgebaut) und **2 Fehlerformaten** (leeres `403.build()`
  vs. JSON via `GlobalExceptionHandler`).
- **Änderung:** Ein Seam `com.xdata.security.CourseAccessGuard` (`requireAdmin` /
  `requireInstructorOrAdmin` / `requireCourseAccess`), wirft `AccessDeniedException` → *ein*
  konsistentes 403-JSON. 7 Controller migriert. Reine Rollen-Gates nutzen weiterhin `@PreAuthorize`.
- **Warum:** Eine Stelle für Policy + Throw + Fehlerformat; konsistente 403-Antwort überall.

---

## 3. Entscheidung: dualer TableMap/Query-Split bleibt (Kandidat 6)

- **Vorher / Vorschlag:** Architektur-Review schlug vor, die zwei Schema-/Query-Welten
  (`util/TableMap` + `legacy/parsing/Query` für SMT-Datengenerierung vs.
  `partialmarking/core/TableMap` + `QueryStructure` für Partial Marking) zu vereinheitlichen.
- **Änderung:** **Bewusst nicht vereinheitlicht.** Begründung in
  [ADR 0001](adr/0001-keep-dual-tablemap-and-query-representations.md): orthogonale Zwecke,
  JDBC- vs. DDL-Aufbau, ~41 eng gekoppelte Legacy-Dateien, kein gemeinsamer Aufrufer, kein Testnetz.
- **Warum:** Sehr hohes Risiko, kein Verhaltensgewinn; ADR verhindert erneutes Vorschlagen.

---

## 4. Frontend: zwei Seams + Migration (Kandidat 7)

- **Vorher:** Die Monaco-SQL-Autocompletion war in 2 Editoren kopiert; Datenladen war als
  hand-gerollte `useState(loading/error/data) + useEffect + try/catch`-Blöcke mit **inkonsistenter**
  Fehlerbehandlung über viele Komponenten verstreut (stilles `catch(()=>{})`, `console.error`-only,
  gemultiplexte `loading`-Flags); Responses als `any`.
- **Änderung:**
  - `src/utils/sqlCompletion.ts` — *eine* reine, unit-getestete `createSqlCompletionProvider`.
  - `src/hooks/useAsyncData.ts` — *ein* Daten-Seam `useAsyncData(fetcher, deps) → {data, loading, error, retry}`.
  - 11 Komponenten migriert (Leaderboard, AuditLogViewer, SchemaManager, DbConnectionManager,
    TestDataViewer, UserManager, InstructorDashboard, AssignmentStats, DatasetPlayground,
    SqlLab, StudentDashboard); Mutationen laden via `retry()` neu.
  - Test-Infra ergänzt: `@testing-library/react`, `@testing-library/dom`, `@types/jest`.
- **Warum:** Duplikation/Inkonsistenz beseitigt; ein einheitlicher, testbarer Lade-/Fehlerpfad.

---

## 5. Test-Schicht (war praktisch nicht vorhanden)

- **Vorher:** Keine Integration-/Controller-/Security-Tests (0 `@SpringBootTest`/`@WebMvcTest`/MockMvc);
  Frontend ungetestet. Die K1/K4-Autorisierung war ungetestet.
- **Änderung:** **11 `@WebMvcTest`-Security-Slice-Tests** über alle Controller (34 Tests), die durch den
  echten Spring-Security-Filter + `@PreAuthorize` + `CourseAccessGuard` + `GlobalExceptionHandler` laufen
  (ohne DB) und unauthentifiziert/falsche-Rolle/Guard-Verweigerung/Happy-Path/konsistentes-403 abdecken.
  Frontend: Tests für `sqlCompletion` + `useAsyncData`.
- **Warum:** Sichert die Autorisierung + Verdrahtung ab; CI-fähig ohne externe Infrastruktur.
  (Muster pro Test: Security-Beans + `JpaMetamodelMappingContext` + ggf. `PlatformTransactionManager`
  mocken; `AccessControlService` steuert den Guard; `@WithMockUser` setzt die Rolle.)

---

## 6. Betrieb / CI / Deployment (Quick Wins)

- **CI:** **Vorher** keine. → **Änderung** `.github/workflows/ci.yml` (Backend `mvn test` auf JDK 21,
  Frontend `npm ci`/`test`/`build`). **Warum:** Tests/Build liefen nirgends automatisch.
- **Docker-Healthchecks:** **Vorher** keine, `depends_on` ohne Bedingung. → **Änderung** Healthchecks
  für db (`pg_isready`) + backend (Port-Probe via `bash /dev/tcp`), `condition: service_healthy`.
  **Warum:** Verhindert Start vor bereiter Abhängigkeit.
- **Debug-Logging:** **Vorher** `JwtAuthenticationFilter` nutzte `System.err.println("[DEBUG_LOG]…")`.
  → **Änderung** `@Slf4j` + `log.debug`. **Warum:** Hygiene.

---

## 7. Schema-Drift behoben (Item #6)

- **Vorher:** Flyway *und* `HIBERNATE_DDL_AUTO=update` mutierten beide das Schema (zwei Wahrheiten);
  Flyway-Flags `validate-on-migrate=false`/`repair-on-migrate=true` versteckten Drift.
- **Änderung:** Striktes Flyway (`validate-on-migrate=true`) + `HIBERNATE_DDL_AUTO=validate` (Flyway =
  einzige Wahrheit). Die strikte Validierung deckte echte Drift auf, die `update` still überdeckte:
  - **V13** — `xdata_audit_logs.id` `serial`→`bigint` (Entity `AuditLog.id` ist `Long`).
  - **V14** — `xdata_password_reset_token.id` `serial`→`bigint` (Entity `Long`).
  - `DbConnection`-Entity: `@Column(name = "connection_id")` ergänzt (mappte fälschlich auf `id`).
- **Warum:** Drift scheitert jetzt **beim Start statt still**; Schema und Entities sind konsistent.
  Verifiziert: Backend bootet sauber mit `validate` gegen ein frisches, nur-von-Flyway gebautes Schema.

---

## 8. Natives Z3 aktiviert / Killing-Data untersucht (Item #5, Pfad A)

- **Vorher:** Die App nutzte **gar kein** natives Z3 — `System.loadLibrary("z3")` schlug fehl → stiller
  CLI-Fallback. `DatasetGenerationService.extractInsertsFromModel` war ein Stub (immer leer).
- **Änderung:**
  - z3-Dependency `com.microsoft:z3:1.0` (vendored, ~z3 4.3.2, keine Natives) → **`tools.aqua:z3-turnkey:4.12.2.1`**
    (bündelt + lädt Natives selbst). `SmtSolverService` erkennt Z3 jetzt via `new Context()`. Kompiliert mit
    0 Fehlern; nativ lädt lokal + im Container (`Z3NativeSmokeTest` als Wächter).
  - **Spike-Ergebnis** (siehe [ADR 0002](adr/0002-killing-data-generation-dormant.md)): Die Killing-Data-
    Pipeline ruht, weil `legacy/database/Configuration.getProperty` ein Stub ist, der für alle Datagen-Keys
    `null` liefert → NPE in `util.TableMap.<init>`. Daher sind sowohl der Testdaten- als auch der SMT-Rung
    schlafend. **Bewusst nicht blind implementiert** (würde falsche Bewertungsdaten erzeugen; seit K1 ist der
    leere Stub sicher → `INCONCLUSIVE`-Fallback).
- **Warum:** Natives Z3 ist das korrekte Fundament; die Killing-Data-Wiederherstellung ist ein
  Rekonstruktionsprojekt (eigene ADR), kein Quick-Fix.

---

## 9. Frontend-Features + Backend-Endpoints entfernt

Auf Wunsch entfernt — nicht-funktionale bzw. nicht gewünschte Features.

- **Bestenliste (Leaderboard):**
  - Frontend: Route `/leaderboard`, Nav-Links, `Leaderboard.tsx`.
  - Backend: `StudentController.getLeaderboard`, `SubmissionAnalytics.leaderboard` (+ `userRepository`),
    `@CacheEvict("leaderboard")`.
- **LMS Sync (kein echtes LTI im Backend):**
  - Frontend: Instructor-Tab + `LmsManager.tsx`.
  - Backend: `LmsController`, `LmsCredentialRepository`, `LmsCredential`, `LmsIntegrationService`,
    `LmsGradePublisher`(Port)+`LmsGradePublisherAdapter`, der `lmsPublisher`-Port aus `gradeNow`,
    Security-Matcher `/api/v1/admin/lms/**`. (DB-Tabelle `xdata_lms_credentials` bleibt als verwaiste
    Tabelle bestehen — `validate` ignoriert Extra-Tabellen.)
- **Testdaten-Viewer** (zeigte immer leere SMT-Testdaten — die Datagen ruht, der Endpoint existierte gar nicht):
  - Frontend: Instructor-Tab + Toggle in `AssignmentManager` + `TestDataViewer.tsx`.
- **SqlLab perfektioniert:** Der nicht-funktionale **SMT-Äquivalenz-Check** (`/playground/smt-check`,
  hängt an der ruhenden Pipeline) wurde entfernt — UI, State, Backend-Endpoint `EvaluationController.checkSmt`
  + `EvaluationService.verifyEquivalence`. Der **funktionierende Partial-Marking-Simulator** bleibt; Hilfetext/
  Button angepasst. (`SmtSolverService.verifyEquivalence` bleibt — vom Grading-`SmtStage` genutzt.)
- **Behalten:** Dataset Playground (auf Wunsch).
- **Warum:** Nur verlässlich funktionierende Features im UI; toten/irreführenden Code entfernt.

---

## Datagen-Rekonstruktion (2026-06-17) — Front-End wiederbelebt

**Vorher:** Die Killing-Data-Pipeline NPE'te beim allerersten Config-Key; der `QueryParser`
scheiterte an **jeder** Query. Das Feature war dormant (ADR 0002).

**Geändert (alle verifiziert, volle Backend-Suite grün):**
- `legacy/database/Configuration`: lädt `util/XData.properties` als Fallback für die 12 Datagen-Keys
  (Laufzeit-Felder behalten Vorrang) → behebt die erste `tempDatabaseType`-NPE.
- `legacy/util/Configuration`: lädt `XData.properties` über absoluten Classpath `/util/XData.properties`
  (lag nicht im Package → vorher NPE für jeden Key).
- `util/TableMap`: Legacy-`Table` wird mit **uppercase** Namen gebaut (PostgreSQL-Identifier sind
  lowercase, Lookups uppercasen → vorher 100 % Parse-Fehler).
- `legacy/parsing/WhereClauseVectorJSQL`: `instanceof Expression` (immer wahr) → `AnyComparisonExpression`
  bei allen Vergleichsoperatoren; Null-Guard für fehlendes GROUP BY.
- `service/DatasetGenerationService`: übergibt `new AppTest_Parameters()` statt `null` an den Parser
  (+ TODO/Hinweis auf die noch fehlende Engine).
- Neuer Golden-Test `DatagenPipelineGoldenTest` (gegen echte PostgreSQL; skippt ohne DB):
  `parsesQualifiedQueryAgainstSchema` (grün, Guard) + `producesNonEmptyDataset` (`@Disabled`, Engine-Grenze).

**Warum:** `Schema → TableMap → QueryParser` funktioniert jetzt end-to-end; das war die am schwersten
zu diagnostizierende, blockierende Schicht.

**Engine läuft end-to-end; Constraint-Korrektheit (a) behoben, Extraktion (b) offen.** Der echte
Datagen-Treiber (`generateDatasetsToKillMutations → GenConstraints → generateDataSetForConstraints →
PopulateTestData.killedMutantsForSMT`) wurde gegen echte PostgreSQL durchgetrieben: SMT erzeugt,
natives Z3 löst, Extraktor läuft.
- **(a) BEHOBEN:** `GenerateJoinPredicateConstraints` verglich Tabellennamen case-sensitiv
  (`getTableName().equals(left)`, `left` lowercased) — durch den TableMap-Uppercase-Fix gebrochen →
  `age>20` landete auf der Subquery-Count-Spalte `JSQ0__XDATA_CNT` → UNSAT. 6 Vergleiche auf
  `equalsIgnoreCase`. Engine liefert jetzt **SAT** mit korrekten Daten (id=1, age=21). Grüner Guard
  `DatagenPipelineGoldenTest.engineGeneratesSatisfiableDatasetConstraints`.
- **(b) BEHOBEN:** Neue `PopulateTestData.generateInsertsFromArrayModel` extrahiert aus dem
  array-kodierten Modell (`O_T : (Array Int T_TupleType)`): liest `model.eval(select(O_T,i))`,
  dekomponiert Tupel-Accessoren, nutzt `XDATA_CNT` als Multiplizität, mappt -99999 → NULL → INSERTs.
  Eingehängt in `cutRequiredOutputForSMTWithAPI` als Fallback. Grüner Guard
  `producesNonEmptyInsertStatements`.

**Pipeline end-to-end funktional:** `Query → Constraints → natives Z3 → Array-Extraktion →
lauffähige INSERTs` (`SELECT id FROM students WHERE age > 20` → `insert into students values (null, 21)`).

**Playground verdrahtet [ERLEDIGT].** `DatasetGenerationService.generateDatasetViaEngine`/`runEngine`
treibt die echte Engine: provisioniert eine **isolierte Wegwerf-PostgreSQL-DB** (`CREATE DATABASE` →
DDL → Engine → `DS*.sql` lesen → `finally`: `DROP DATABASE … WITH (FORCE)` + Temp-Dir löschen),
serialisiert über statischen `ENGINE_LOCK`, `TableMap.clearAllInstances()` pro Request. **Live über
die HTTP-API verifiziert:** Schema hochladen → `generate-killing-data` →
`{success:true, inserts:["insert into students values (null, 21);"]}`; Temp-DBs aufgeräumt (0 Leak).
Scope: nur Playground (Grading unverändert auf `INCONCLUSIVE`-Fallback); Queries brauchen
tabellenqualifizierte Spalten.

**Gezielte Killing-Data (`mutationTypes`) verdrahtet.** `runEngine` erzeugt Basis-Datensatz + je
angefordertem Mutationstyp einen Killing-Datensatz (`SELECTION`/`EQUIVALENCE`/`NONEQUIJOIN`/`AGG`/
`DISTINCT`/`EXTRAGROUPBY`/`HAVING` → `TagDatasets` → `generateConstraintsToKillMutations`, je Typ
eigener try/catch). Live: `age>20` mit `["SELECTION","AGG","HAVING","FOO"]` → `(null,21)`, `(4,20)`
(Grenzwert tötet »age>=20«), `(4,-1)`; `FOO` übersprungen. Guard
`DatagenPipelineGoldenTest.drivesFullMutationKillingSuite`. `mutantQuery` (1 konkreter Mutant) noch offen.
Details: ADR 0002 + `dataset-playground-analysis.md`.

---

## Verifikationsstand (Ende der Sitzung)
- Backend: **83 Tests grün** (Datagen-Engine end-to-end inkl. INSERT-Extraktion); bootet `healthy` mit striktem `validate` + nativem Z3.
- Frontend: **7 Tests grün**; CI-Build (`CI=true`) warnungsfrei.
- Alle Docker-Builds (db/backend/frontend) erfolgreich; entfernte Endpoints nicht mehr gemappt,
  behaltene (`/admin/courses`, `/playground/partial-marking`) liefern 200.

## Offene, dokumentierte Punkte
- Killing-Data-Generierung: **vollständig wiederhergestellt + im Playground live verdrahtet inkl. gezielter `mutationTypes`** (Config + Parser + Constraints + Z3 + INSERT-Extraktion + isolierte Temp-PG-DB + Mutanten-Killer). Grading bleibt bewusst auf `INCONCLUSIVE`-Fallback; `mutantQuery` (1 konkreter Mutant) noch offen — ADR 0002.
- Verwaiste Tabelle `xdata_lms_credentials`: optionale `DROP TABLE`-Migration (V15) noch nicht angelegt.
- Entfernte Pfade liefern 500 statt 404 (vorbestehender catch-all-`Exception`-Handler).
- Weitere „was fehlt"-Punkte (Secrets-Defaults, Input-Validation, Rate-Limiting, Pagination, Actuator)
  sind noch offen.
