# Analyse: Dataset Playground & SMT-Datensatzgenerierung (2026-06-17)

Vollständige Analyse der Dataset-Playground-Funktion und der dahinterliegenden
SMT-Solver-Datengenerierung, **live im Container reproduziert**. Ergänzt
[ADR 0002](adr/0002-killing-data-generation-dormant.md).

## Komponenten-Kette

**Frontend** `src/components/DatasetPlayground.tsx`
- `GET /api/v1/schemas` → Schema-Dropdown.
- `POST /api/v1/playground/upload-schema` (multipart) → Schema hochladen.
- `POST /api/v1/playground/generate-killing-data` `{query, mutantQuery, schemaId, mutationTypes}`.

**Backend** `DatasetPlaygroundController` (`/api/v1/playground`)
- `uploadSchema` → `FileStorageService.storeFile` + `SchemaService.saveSchema`.
- `generateKillingData` → `DatasetGenerationService.generateDatasetFromQuery(query, mutantQuery, schemaId, mutationTypes)`
  → `generateDatasetFromQuery(query, schemaId)`
  → `MetadataService.getLegacyTableMap(schemaId)` → `util.TableMap.getInstances` → `new QueryParser(...)`
  → `GenerateCVC1.inititalizeSQDataset()` / `getCVCStr()` → `SmtSolverService.solveDetailed(smt)`
  → `extractInsertsFromModel(model, tableMap)`.

## Live-Test-Ergebnisse (admin1, frisches DB)

| Schritt | Ergebnis |
|---|---|
| `upload-schema` (students.sql) | ✅ **funktioniert** — Schema gespeichert, `schemaId=1`, in `/schemas` sichtbar |
| `GET /schemas` | ✅ funktioniert |
| `generate-killing-data` (`SELECT id FROM students WHERE age > 20`, schemaId=1) | ⚠️ (damals) HTTP 200, aber `{"success":true,"inserts":[],...}` — **inzwischen behoben**, siehe „Rekonstruktions-Fortschritt" unten |

> **Hinweis (Update):** Die unten dokumentierte Rekonstruktion hat diese Funktion repariert —
> der Endpoint liefert jetzt echte INSERTs (`insert into students values (null, 21)`). Der
> folgende Abschnitt beschreibt den **ursprünglichen** kaputten Zustand zur Nachvollziehbarkeit.

Damals zeigte das Frontend den Toast **„Dataset generated successfully"** und eine leere
Liste → **irreführender Erfolg**.

## Fehlerkette (reproduziert aus den Backend-Logs)

```
DatasetGenerationService.generateDatasetFromQuery
  → MetadataService.getLegacyTableMap(1)
    → util.TableMap.getInstances(conn, 1)
      → util.TableMap.<init>  (TableMap.java:125)
        → Configuration.getProperty("tempDatabaseType").equalsIgnoreCase("sqlite")
          → NullPointerException   (getProperty liefert null)
```
Der NPE wird in `generateDatasetFromQuery`s `catch (Exception)` **verschluckt** → leere Liste
→ Controller meldet `success=true, "No data could be generated"`.

## Ursachen (mehrschichtig)

1. **Legacy-`Configuration` ist ein Stub.** `legacy/database/Configuration.getProperty` kennt nur
   **8** Keys (databaseName, existingDatabaseUser, …, homeDir). Der Datagen-Pfad verlangt aber u. a.:
   `tempDatabaseType, cntFlag, isEnumInt, smtsolver, tempJoins, regressDS0, existsUnrollFlag,
   enumArrayIndex, primarykey, printDir, printSQL, sampleDataJson` — **alle liefern `null`**.
   Erster Treffer: `tempDatabaseType` → NPE. (Weitere folgen, sobald dieser behoben ist.)
2. **`extractInsertsFromModel` ist ein Stub.** Selbst mit gesetzter Config + SAT-Model gibt die Methode
   **immer `[]`** zurück (Kommentare „statische Testdaten", „Dummy-Daten").
3. **Model-Format-Mismatch.** z3 4.x liefert das Model als verschachtelte Array-`store`-S-Expression;
   der Legacy-String-Parser erwartet flache `(<T>_TupleType v..)`-Zeilen. Der dafür gedachte
   API-Extraktor braucht das Z3-**Model-Objekt** (jetzt via nativem z3-turnkey verfügbar — siehe Item 8 in
   [CHANGES.md](CHANGES.md)).
4. **Eingaben werden ignoriert.** `mutantQuery` und `mutationTypes` haben **keine Wirkung**
   (Log: „Mutant query and mutation types are currently ignored in this simplified version").

## Auswirkung / Schweregrad

- **Dataset Playground erzeugt nie Daten.** Kein Absturz (HTTP 200), aber die UI suggeriert Erfolg →
  irreführend für Nutzer.
- `upload-schema` + Schema-Auswahl funktionieren.
- Dieselbe Wurzel (Configuration-Stub) legt auch den Testdaten- **und** SMT-Rung im Grading lahm
  (ADR 0002) — dort jedoch **sicher** abgefangen (`INCONCLUSIVE`-Fallback, kein Falsch-Score).

## Was ein vollständiger Fix erfordert (Rekonstruktionsprojekt, ADR 0002)

1. Legacy-`Configuration` mit **korrekten** Werten für die 12 fehlenden Keys füllen
   (XData-internes Wissen, z. B. `tempDatabaseType`, `cntFlag`, `isEnumInt`, …).
2. `GenerateCVC1` auf modernem z3 verifizieren (parsebares SAT-Model für ein reales Schema+Query).
3. `extractInsertsFromModel` über das **native Z3-`Model`-Objekt** implementieren (Array/`FuncInterp`-fähig).
4. Golden-End-to-End-Test (bekanntes Schema+Query → INSERTs, die auf dem Schema laufen).
5. `mutantQuery`/`mutationTypes` verdrahten — oder die UI auf das reduzieren, was tatsächlich wirkt.

## Sofort-Empfehlung (klein, ohne Reconstruction)

Den **irreführenden „success"** ehrlich machen: bei leerem Ergebnis nicht „erfolgreich generiert",
sondern z. B. „Datengenerierung derzeit nicht verfügbar" melden (oder das Feature als *experimentell*
kennzeichnen / vorerst ausblenden), damit Nutzer nicht von funktionierender Generierung ausgehen.
*(Umgesetzt — Frontend-Toast + Backend-Meldung sind jetzt ehrlich.)*

## Rekonstruktions-Fortschritt (2026-06-17)

Eine Rekonstruktionsrunde hat die Pipeline **von „NPE beim ersten Config-Key" bis durch das
komplette Query-Parsing** gebracht. Behoben (alle verifiziert; volle Backend-Suite grün, 82 Tests):

1. `legacy/database/Configuration` lädt jetzt die gebündelte `util/XData.properties` für die 12
   Datagen-Keys (Laufzeit-Felder behalten Vorrang). Behebt die erste NPE (`tempDatabaseType`).
2. `legacy/util/Configuration` (ein **zweiter**, separater Stub, den `GenerateCVC1` nutzt) lädt
   `XData.properties` jetzt über den absoluten Classpath `/util/XData.properties` statt relativ
   zum Package (dort lag die Datei nicht → NPE für jeden Key).
3. `TableMap.createTableMap` baut die Legacy-`Table` jetzt mit **uppercase** Namen. PostgreSQL
   faltet Identifier klein; der Parser schlägt aber via `getFromTables().get(name.toUpperCase())`
   nach, während `addFromTable` per `table.getTableName()` ablegt → jeder Spalten-Lookup ging
   daneben, **jede Query scheiterte am Parsen**.
4. `WhereClauseVectorJSQL`: der `> ALL(...)`-Subquery-Zweig jedes Vergleichsoperators prüfte
   `instanceof Expression` (in JSQLParser **immer wahr**) → `col > 20` lief fälschlich rein und
   NPE'te. Guard → `instanceof AnyComparisonExpression`.
5. `WhereClauseVectorJSQL.getAggregationDataStructures`: Null-Guard für fehlendes GROUP BY.
6. Der Parser braucht ein **nicht-null `AppTest_Parameters`** und **tabellenqualifizierte Spalten**
   (`students.id`). Beides wird jetzt von `DatasetGenerationService` gesetzt.

**Netto:** `Schema-Metadaten → TableMap → QueryParser` funktioniert end-to-end auf PostgreSQL
(war zu 100 % kaputt). Guard: `DatagenPipelineGoldenTest.parsesQualifiedQueryAgainstSchema`.

**Verbleibende Grenze (die Engine) — läuft jetzt end-to-end, 2 Korrektheits-Bugs offen.**
Die eigentliche Constraint-Generierung + Solve + Extraktion ist **nicht** die naive
`inititalizeSQDataset()+getCVCStr()`-Sequenz (beide liefern `null`), sondern der Treiber
`generateDatasetsToKillMutations() → GenConstraints → generateDataSetForConstraints →
PopulateTestData.killedMutantsForSMT`. Dieser Flow wurde gegen echte PostgreSQL **komplett
durchgetrieben** (cvc mit JDBC-Connection, `temp_smt/` angelegt, `buildQueryStructureJSQL → …
→ generateDatasetForNonEmptyDataset`) — **läuft ohne Crash**: SMT wird erzeugt, natives Z3
löst, der Extraktor läuft. Zwei Korrektheits-Bugs bleiben (root-caused, Resume-Harness
`DatagenPipelineGoldenTest.producesNonEmptyDataset`):

1. **WHERE-Selektion falsch gemappt (`outerSQ=true`) → UNSAT. [BEHOBEN]** `age > 20` landete auf
   der Subquery-Count-Spalte `JSQ0__XDATA_CNT` statt `JSQ0_STUDENTS__AGE1` → UNSAT. Ursache: der
   *gleiche* Case-Mismatch wie beim Parser — `GenerateJoinPredicateConstraints` verglich
   `table.getTableName().equals(left)` mit lowercased `left`, während der TableMap-Fix
   `getTableName()` uppercase machte → kein Treffer → Spaltenindex lief auf die Count-Spalte.
   Fix: 6 Tabellennamen-Vergleiche auf `equalsIgnoreCase`. Engine liefert jetzt ein **SAT**-Modell
   mit korrekten Daten (`(STUDENTS_TupleType 1 21 1)` → id=1, age=21). Guard:
   `DatagenPipelineGoldenTest.engineGeneratesSatisfiableDatasetConstraints`.
2. **Array-kodiertes Modell braucht einen Array-fähigen Extraktor. [BEHOBEN]** Neue Methode
   `PopulateTestData.generateInsertsFromArrayModel`: liest pro `O_T` das Modell-Array
   (`model.eval(select(O_T,i))`), dekomponiert die Tupel-Accessoren, nutzt `XDATA_CNT` als
   Zeilen-Multiplizität, mappt -99999 → NULL und erzeugt INSERTs.

**Pipeline jetzt end-to-end funktional:** `Query → Constraints → natives Z3 → Array-Extraktion →
lauffähige INSERTs`, bewiesen durch `DatagenPipelineGoldenTest.producesNonEmptyInsertStatements`
(`SELECT id FROM students WHERE age > 20` → `insert into students values (null, 21)`).

**Playground verdrahtet [ERLEDIGT].** `DatasetGenerationService.generateDatasetViaEngine` treibt
jetzt die echte Engine. Da das Legacy-`TableMap`/`GenerateCVC1` PostgreSQL-nativ und mit der
Derby-Scratch-DB inkompatibel ist, provisioniert `runEngine` eine **isolierte Wegwerf-PostgreSQL-DB**
(`CREATE DATABASE` → DDL anwenden → Engine treiben → `DS*.sql` lesen → im `finally` `DROP DATABASE
… WITH (FORCE)` + Temp-Dir löschen). Serialisiert über einen statischen `ENGINE_LOCK` (gemeinsamer
`ConstraintGenerator.ctx` + `Configuration.homeDir`); `TableMap.clearAllInstances()` pro Request.

**Live verifiziert** über die HTTP-API: Schema `students (id, age)` hochladen, dann
`POST /playground/generate-killing-data` mit `SELECT students.id FROM students WHERE students.age > 20`
→ `{success:true, inserts:["insert into students values (null, 21);"]}`. Temp-DBs werden aufgeräumt
(0 übrig nach wiederholten Aufrufen).

**Gezielte Killing-Data (`mutationTypes`) verdrahtet.** `runEngine` erzeugt den Basis-Datensatz
**plus** je angefordertem Mutationstyp einen Killing-Datensatz (`SELECTION`, `EQUIVALENCE`,
`NONEQUIJOIN`, `AGG`, `DISTINCT`, `EXTRAGROUPBY`, `HAVING` → `TagDatasets` →
`generateConstraintsToKillMutations`), jeder Typ in eigenem try/catch. Live verifiziert:
`SELECT students.id FROM students WHERE students.age > 20` mit `["SELECTION","AGG","HAVING","FOO"]`
→ `(null, 21)` (Basis), `(4, 20)` (Grenzwert, tötet den Mutanten »age >= 20«), `(4, -1)`;
`FOO` übersprungen, AGG/HAVING ohne Ziel → nichts.

**Scope:** nur der Playground-Pfad; das Grading bleibt auf dem sicheren `INCONCLUSIVE`-Fallback.
Queries müssen **tabellenqualifizierte Spalten** nutzen. `mutantQuery` (Daten, die die Query von
*einem* konkreten Mutanten unterscheiden) ist ein anderer Pfad und noch nicht verdrahtet.
Details: [ADR 0002](adr/0002-killing-data-generation-dormant.md).
