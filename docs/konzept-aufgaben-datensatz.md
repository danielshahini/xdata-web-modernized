# Konzept: Pro Aufgabe ein Schema + EIN gespeicherter Datensatz (SMT) & externe Generierung

> Konzept-/Designdokument (noch keine Umsetzung). Beantwortet zwei Fragen:
> 1. Eine Aufgabe an **ein** Schema binden und den Datensatz **einmal** vom Lehrer (per SMT-Solver)
>    generieren lassen — statt bei jeder Abgabe neu. Sinnvoll?
> 2. Die schwere Mutanten-/Daten-Generierung **extern** laufen lassen und den Lehrer-Prozess vereinfachen.

## Einschätzung — kurz: ja, beides ist genau richtig
Die Architektur unterstützt es bereits. Grading kann **ohne Live-Datenbank** auf einer Wegwerf-DB laufen:
`TestExecutionService.compareQueries(refQuery, studentQuery, datensatz, schemaId)` baut aus der Schema-DDL
(`SchemaInfo.content`) + einer Liste INSERTs eine **ephemere Derby-In-Memory-DB**
(`db/ScratchDatabaseFactory.create(ddl, testData)`), führt beide Queries aus und vergleicht als Multiset
(`db/ResultRows.matches`). Heute ruft `eval/adapter/TestDataExecutionStage` die Generierung jedoch **pro
Abgabe** auf (`DatasetGenerationService.runEngine` — schwer, per `ENGINE_LOCK` serialisiert, 60s-Timeout,
bei Joins fragil) und fällt meist auf die Live-DB-Stufe (`AssignedDbStage`) zurück.

→ Einen **fixen Datensatz pro Aufgabe** zu speichern und in genau diese Stufe einzuspeisen ist billiger,
deterministisch (alle Studierenden auf denselben Daten bewertet) und nimmt die schwere Engine aus dem
Grading-Hot-Path.

## Ist-Stand des Gradings
- Kette in `eval/SubmissionEvaluationService`: TextMatch → **TestData** → SMT → **AssignedDb**; erste
  entscheidende Stufe gewinnt; parallel das strukturelle Partial-Marking.
- `TestDataExecutionStage` braucht nur `schemaId` + `List<String>` INSERTs (Scratch-DB) — **nie** die
  Live-Connection. `AssignedDbStage` nutzt die Live-`connection` der Aufgabe.
- `Assignment` hat `defaultSchemaId` + `connection`; `SchemaInfo` hat nur die DDL (`content`).
  **Es gibt heute keine Datensatz-Persistenz.**

## Konzept A — Aufgabe = 1 Schema + 1 gespeicherter Datensatz
**Persistenz (zwei Optionen):**
- *Einfach (v1):* Spalte `seed_sql TEXT` auf `xdata_assignment` (INSERTs als ein SQL-Block).
- *Sauberer (mittelfristig):* eigene Tabelle `xdata_assignment_dataset`
  (`assignment_id`, `dataset_sql`, `source_query`, `mutation_types`, `generated_at`, `created_by`) →
  Versionierung/Audit, mehrere Datensätze pro Aufgabe.

**Grading-Seam (minimaler Eingriff):** den gespeicherten Datensatz durch `EvaluationRequest`/`SchemaRef`
reichen; in `TestDataExecutionStage` zuerst den **gespeicherten** Datensatz nutzen statt zu generieren →
`compareQueries(ref, student, gespeicherterDatensatz, schemaId)` auf der Scratch-DB → entscheidendes
EQUIVALENT/NOT_EQUIVALENT. Reihenfolge: gespeicherter Datensatz → (optional) Live-DB als Zweitcheck →
(optional) On-the-fly-Generierung nur als letzter Fallback.

**Vorteile:** deterministisch, schnell, kein `ENGINE_LOCK` im Bewertungspfad, **keine Live-DB pro Aufgabe
nötig** (entkoppelt vom Aufsetzen/Seeden einer externen Kurs-DB).

**Ehrlicher Tradeoff/Risiko:** Ein einzelner fixer „Killing-Datensatz" erkennt nur die Mutationen, für die
er erzeugt wurde → eine zufällig falsche Studenten-Query könnte auf genau diesen Daten „korrekt" wirken
(False Positive). Gegenmaßnahmen: Datensatz als **Killing-Set für die Mutanten der Referenz-Query** erzeugen;
ggf. mehrere Datensätze; Live-DB als optionalen Zweitcheck behalten; **Datensatz invalidieren/neu erzeugen,
wenn Referenz-Query/Schema sich ändern**. Außerdem Derby-Dialekt-Kompatibilität der DDL/INSERTs beachten
(Postgres-Eigenheiten wie `SERIAL`/`LIMIT`).

## Konzept B — Generierung vereinfachen & extern auslagern
Weil die Z3-Engine schwer/seriell/fragil ist: **Generierung von Speichern+Grading trennen.**
- **In-App-Generieren je Aufgabe:** Knopf „Testdaten generieren" im Aufgaben-Wizard, der die vorhandene
  Engine (`DatasetPlaygroundController` `/generate-killing-data`) für die Referenz-Queries nutzt, Vorschau
  zeigt und an die Aufgabe speichert.
- **Extern auslagern:** „Generier-Job exportieren" — Bundle aus Schema-DDL + Referenz-Queries +
  Mutationstypen (JSON/`.sql`) herunterladen; der Lehrer lässt die Generierung auf einer **anderen
  Plattform** laufen (Standalone-Runner / dieselbe Engine offline / eigenes Tooling); danach **Ergebnis
  importieren** (`.sql`-Upload/Einfügen) → an der Aufgabe gespeichert. Wiederverwendbar:
  `FileStorageService` + Upload-Muster `/playground/upload-schema`.
- **Simpelster Weg:** „eigene Seed-SQL einfügen/hochladen".

## Vereinfachter Lehrer-Workflow (Zielbild)
1. Schema wählen (1 pro Aufgabe — `defaultSchemaId`, existiert).
2. Referenz-Queries schreiben (existiert).
3. **Datensatz beschaffen** — drei gleichwertige Wege im selben Schritt: in-app generieren / extern
   generieren → importieren / eigene Seed-SQL einfügen.
4. **Vorschau + Validierung:** Referenz-Query auf dem Datensatz ausführen (nicht leer? plausibel?).
5. Speichern → ab dann **schnelles, deterministisches Grading** ohne Pro-Abgabe-Generierung/Live-DB.

## Wiederverwendbare Bausteine (für eine spätere Umsetzung)
- Grading auf Wegwerf-DB: `service/TestExecutionService.compareQueries`, `db/ScratchDatabaseFactory`
  (Derby in-memory), `db/QueryRunner`, `db/ResultRows.matches`, `eval/adapter/TestDataExecutionStage`.
- Generierung: `service/DatasetGenerationService.runEngine`, `controller/DatasetPlaygroundController` +
  `dto/DatasetPlaygroundRequest/Response`, Mutationstypen (SELECTION/EQUIVALENCE/AGG/DISTINCT/…).
- Import/Upload: `service/FileStorageService`, Muster `/playground/upload-schema`.
- Persistenz: `model/Assignment` (neue Spalte/Tabelle), `model/SchemaInfo` (DDL vorhanden), Flyway
  (nächste Version V24).
- UI: `components/AssignmentManager.tsx` (neuer „Testdaten"-Schritt), `components/DatasetPlayground.tsx`
  (Generier-UI + Copy/Download als Vorlage/Verknüpfung).

## Phasen-Roadmap
- **Phase A** (Kern, mittel): `seed_sql` an Aufgabe + Grading nutzt gespeicherten Datensatz + Import/Paste-UI.
  Größter Nutzen, niedriges Risiko (nutzt die getestete Scratch-DB-Seam).
- **Phase A+**: „Für diese Aufgabe generieren"-Knopf (Engine-Wiederverwendung).
- **Phase B** (extern): Export-Job + Import-Ergebnis (Offload) für die schweren/fragilen Fälle.

## Offene Entscheidungen
- Persistenz: Spalte vs. eigene Tabelle (Versionierung?).
- Live-DB (`AssignedDbStage`) als Zweitcheck behalten oder ganz auf Datensätze umstellen?
- Datensatz-Invalidierung bei Query-/Schema-Änderung (automatisch „veraltet" markieren?).
- Format des Export-Jobs (JSON-Schema) + ob ein offizieller Standalone-Runner bereitgestellt wird.
</content>
