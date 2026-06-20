# Dataset Playground — Härtetest-Report (semantisch)

Datum: 2026-06-20 · Fälle gesamt: **181** · Schemas: 6 · Mutationstypen: 7

## Kennzahlen

- HTTP 200: **74/181** · kein 5xx/Verbindungsfehler: **74/181**
- Nicht-leere INSERTs erzeugt (type/kill-Läufe): **58/163**
- INSERTs valide & ladbar: **46/52**
- Referenz-Query liefert ≥1 Zeile auf generierten Daten: **16/46**
- Konstruierter Mutant **gekillt** (Daten unterscheiden Referenz≠Mutant): **1/4**
- Stabilität (5× Wiederholung, nicht-leer): **0/5**

## Auffällige Fälle

- `single` / type:SELECTION — `SELECT id FROM nums WHERE val > 20` → LOAD-FEHLER: DETAIL:  Key (id)=(4) already exists.
- `single` / kill:SELECTION — `SELECT id FROM nums WHERE val > 20` → LOAD-FEHLER: DETAIL:  Key (id)=(4) already exists.
- `single` / type:SELECTION — `SELECT id FROM nums WHERE val > 20 AND label = 'x'` → LEER
- `single` / type:EQUIVALENCE — `SELECT id FROM nums WHERE val > 20 AND label = 'x'` → LEER
- `single` / type:NONEQUIJOIN — `SELECT id FROM nums WHERE val > 20 AND label = 'x'` → LEER
- `single` / type:AGG — `SELECT id FROM nums WHERE val > 20 AND label = 'x'` → LEER
- `single` / type:DISTINCT — `SELECT id FROM nums WHERE val > 20 AND label = 'x'` → LEER
- `single` / type:EXTRAGROUPBY — `SELECT id FROM nums WHERE val > 20 AND label = 'x'` → LEER
- `single` / type:HAVING — `SELECT id FROM nums WHERE val > 20 AND label = 'x'` → LEER
- `single` / kill:SELECTION — `SELECT id FROM nums WHERE val > 20 AND label = 'x'` → LEER
- `single` / type:ALL — `SELECT id FROM nums WHERE val > 20 AND label = 'x'` → LEER
- `single` / type:SELECTION — `SELECT DISTINCT label FROM nums` → Referenz LEER auf Daten
- `single` / type:EQUIVALENCE — `SELECT DISTINCT label FROM nums` → Referenz LEER auf Daten
- `single` / type:NONEQUIJOIN — `SELECT DISTINCT label FROM nums` → Referenz LEER auf Daten
- `single` / type:AGG — `SELECT DISTINCT label FROM nums` → Referenz LEER auf Daten
- `single` / type:DISTINCT — `SELECT DISTINCT label FROM nums` → Referenz LEER auf Daten
- `single` / type:EXTRAGROUPBY — `SELECT DISTINCT label FROM nums` → Referenz LEER auf Daten
- `single` / type:HAVING — `SELECT DISTINCT label FROM nums` → Referenz LEER auf Daten
- `single` / kill:DISTINCT — `SELECT DISTINCT label FROM nums` → Referenz LEER auf Daten; NICHT gekillt
- `uni` / type:SELECTION — `SELECT name FROM students WHERE age > 22` → LOAD-FEHLER: DETAIL:  Key (id)=(4) already exists.
- `uni` / type:EQUIVALENCE — `SELECT name FROM students WHERE age > 22` → Referenz LEER auf Daten
- `uni` / type:NONEQUIJOIN — `SELECT name FROM students WHERE age > 22` → Referenz LEER auf Daten
- `uni` / type:AGG — `SELECT name FROM students WHERE age > 22` → Referenz LEER auf Daten
- `uni` / type:DISTINCT — `SELECT name FROM students WHERE age > 22` → Referenz LEER auf Daten
- `uni` / type:EXTRAGROUPBY — `SELECT name FROM students WHERE age > 22` → Referenz LEER auf Daten
- `uni` / type:HAVING — `SELECT name FROM students WHERE age > 22` → Referenz LEER auf Daten
- `uni` / kill:SELECTION — `SELECT name FROM students WHERE age > 22` → LOAD-FEHLER: DETAIL:  Key (id)=(4) already exists.
- `uni` / type:SELECTION — `SELECT s.name FROM students s JOIN enroll e ON s.id = e.sid` → Referenz LEER auf Daten
- `uni` / type:EQUIVALENCE — `SELECT s.name FROM students s JOIN enroll e ON s.id = e.sid` → Referenz LEER auf Daten
- `uni` / type:NONEQUIJOIN — `SELECT s.name FROM students s JOIN enroll e ON s.id = e.sid` → Referenz LEER auf Daten
- `uni` / type:AGG — `SELECT s.name FROM students s JOIN enroll e ON s.id = e.sid` → Referenz LEER auf Daten
- `uni` / type:DISTINCT — `SELECT s.name FROM students s JOIN enroll e ON s.id = e.sid` → Referenz LEER auf Daten
- `uni` / type:EXTRAGROUPBY — `SELECT s.name FROM students s JOIN enroll e ON s.id = e.sid` → Referenz LEER auf Daten
- `uni` / type:HAVING — `SELECT s.name FROM students s JOIN enroll e ON s.id = e.sid` → Referenz LEER auf Daten
- `uni` / kill:EQUIVALENCE — `SELECT s.name FROM students s JOIN enroll e ON s.id = e.sid` → Referenz LEER auf Daten; NICHT gekillt
- `uni` / type:EXTRAGROUPBY — `SELECT major, COUNT(*) FROM students GROUP BY major` → LOAD-FEHLER: DETAIL:  A field with precision 3, scale 2 must round to an absolute value less than 10^1.
- `uni` / kill:EXTRAGROUPBY — `SELECT major, COUNT(*) FROM students GROUP BY major` → LOAD-FEHLER: DETAIL:  A field with precision 3, scale 2 must round to an absolute value less than 10^1.
- `uni` / type:SELECTION — `SELECT major FROM students GROUP BY major HAVING COUNT(*) > 1` → Referenz LEER auf Daten
- `uni` / type:EQUIVALENCE — `SELECT major FROM students GROUP BY major HAVING COUNT(*) > 1` → Referenz LEER auf Daten
- `uni` / type:NONEQUIJOIN — `SELECT major FROM students GROUP BY major HAVING COUNT(*) > 1` → Referenz LEER auf Daten
- `uni` / type:AGG — `SELECT major FROM students GROUP BY major HAVING COUNT(*) > 1` → Referenz LEER auf Daten
- `uni` / type:DISTINCT — `SELECT major FROM students GROUP BY major HAVING COUNT(*) > 1` → Referenz LEER auf Daten
- `uni` / type:EXTRAGROUPBY — `SELECT major FROM students GROUP BY major HAVING COUNT(*) > 1` → Referenz LEER auf Daten
- `uni` / type:HAVING — `SELECT major FROM students GROUP BY major HAVING COUNT(*) > 1` → Referenz LEER auf Daten
- `uni` / kill:HAVING — `SELECT major FROM students GROUP BY major HAVING COUNT(*) > 1` → Referenz LEER auf Daten; NICHT gekillt
- `types` / type:AGG — `SELECT id FROM mix WHERE amount > 100.00` → HTTP 0; LEER
- `types` / type:DISTINCT — `SELECT id FROM mix WHERE amount > 100.00` → HTTP 0; LEER
- `types` / type:EXTRAGROUPBY — `SELECT id FROM mix WHERE amount > 100.00` → HTTP 0; LEER
- `types` / type:HAVING — `SELECT id FROM mix WHERE amount > 100.00` → HTTP 0; LEER
- `types` / kill:SELECTION — `SELECT id FROM mix WHERE amount > 100.00` → HTTP 0; LEER
- `types` / type:ALL — `SELECT id FROM mix WHERE amount > 100.00` → HTTP 0; LEER
- `types` / custom-mutant — `SELECT id FROM mix WHERE amount > 100.00` → HTTP 0
- `types` / type:SELECTION — `SELECT id FROM mix WHERE active = true` → HTTP 0; LEER
- `types` / type:EQUIVALENCE — `SELECT id FROM mix WHERE active = true` → HTTP 0; LEER
- `types` / type:NONEQUIJOIN — `SELECT id FROM mix WHERE active = true` → HTTP 0; LEER
- `types` / type:AGG — `SELECT id FROM mix WHERE active = true` → HTTP 0; LEER
- `types` / type:DISTINCT — `SELECT id FROM mix WHERE active = true` → HTTP 0; LEER
- `types` / type:EXTRAGROUPBY — `SELECT id FROM mix WHERE active = true` → HTTP 0; LEER
- `types` / type:HAVING — `SELECT id FROM mix WHERE active = true` → HTTP 0; LEER
- `types` / kill:SELECTION — `SELECT id FROM mix WHERE active = true` → HTTP 0; LEER
- `types` / type:ALL — `SELECT id FROM mix WHERE active = true` → HTTP 0; LEER
- `types` / custom-mutant — `SELECT id FROM mix WHERE active = true` → HTTP 0
- `types` / type:SELECTION — `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → HTTP 0; LEER
- `types` / type:EQUIVALENCE — `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → HTTP 0; LEER
- `types` / type:NONEQUIJOIN — `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → HTTP 0; LEER
- `types` / type:AGG — `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → HTTP 0; LEER
- `types` / type:DISTINCT — `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → HTTP 0; LEER
- `types` / type:EXTRAGROUPBY — `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → HTTP 0; LEER
- `types` / type:HAVING — `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → HTTP 0; LEER
- `types` / kill:SELECTION — `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → HTTP 0; LEER
- `types` / type:ALL — `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → HTTP 0; LEER
- `types` / custom-mutant — `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → HTTP 0
- `emp` / type:SELECTION — `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary ` → HTTP 0; LEER
- `emp` / type:EQUIVALENCE — `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary ` → HTTP 0; LEER
- `emp` / type:NONEQUIJOIN — `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary ` → HTTP 0; LEER
- `emp` / type:AGG — `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary ` → HTTP 0; LEER
- `emp` / type:DISTINCT — `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary ` → HTTP 0; LEER
- `emp` / type:EXTRAGROUPBY — `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary ` → HTTP 0; LEER
- `emp` / type:HAVING — `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary ` → HTTP 0; LEER
- `emp` / kill:SELECTION — `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary ` → HTTP 0; LEER
- `emp` / type:ALL — `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary ` → HTTP 0; LEER
- `emp` / custom-mutant — `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE e.salary ` → HTTP 0
- `emp` / type:SELECTION — `SELECT did, AVG(salary) FROM emp GROUP BY did` → HTTP 0; LEER
- `emp` / type:EQUIVALENCE — `SELECT did, AVG(salary) FROM emp GROUP BY did` → HTTP 0; LEER
- `emp` / type:NONEQUIJOIN — `SELECT did, AVG(salary) FROM emp GROUP BY did` → HTTP 0; LEER
- `emp` / type:AGG — `SELECT did, AVG(salary) FROM emp GROUP BY did` → HTTP 0; LEER
- `emp` / type:DISTINCT — `SELECT did, AVG(salary) FROM emp GROUP BY did` → HTTP 0; LEER
- `emp` / type:EXTRAGROUPBY — `SELECT did, AVG(salary) FROM emp GROUP BY did` → HTTP 0; LEER
- `emp` / type:HAVING — `SELECT did, AVG(salary) FROM emp GROUP BY did` → HTTP 0; LEER
- `emp` / kill:AGG — `SELECT did, AVG(salary) FROM emp GROUP BY did` → HTTP 0; LEER
- `emp` / type:ALL — `SELECT did, AVG(salary) FROM emp GROUP BY did` → HTTP 0; LEER
- `emp` / custom-mutant — `SELECT did, AVG(salary) FROM emp GROUP BY did` → HTTP 0
- `emp` / type:SELECTION — `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2` → HTTP 0; LEER
- `emp` / type:EQUIVALENCE — `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2` → HTTP 0; LEER
- `emp` / type:NONEQUIJOIN — `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2` → HTTP 0; LEER
- `emp` / type:AGG — `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2` → HTTP 0; LEER
- `emp` / type:DISTINCT — `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2` → HTTP 0; LEER
- `emp` / type:EXTRAGROUPBY — `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2` → HTTP 0; LEER
- `emp` / type:HAVING — `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2` → HTTP 0; LEER
- `emp` / kill:HAVING — `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2` → HTTP 0; LEER
- `emp` / type:ALL — `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2` → HTTP 0; LEER
- `emp` / custom-mutant — `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) > 2` → HTTP 0
- `nullable` / type:SELECTION — `SELECT name FROM people WHERE age > 30` → HTTP 0; LEER
- `nullable` / type:EQUIVALENCE — `SELECT name FROM people WHERE age > 30` → HTTP 0; LEER
- `nullable` / type:NONEQUIJOIN — `SELECT name FROM people WHERE age > 30` → HTTP 0; LEER
- `nullable` / type:AGG — `SELECT name FROM people WHERE age > 30` → HTTP 0; LEER
- `nullable` / type:DISTINCT — `SELECT name FROM people WHERE age > 30` → HTTP 0; LEER
- `nullable` / type:EXTRAGROUPBY — `SELECT name FROM people WHERE age > 30` → HTTP 0; LEER
- `nullable` / type:HAVING — `SELECT name FROM people WHERE age > 30` → HTTP 0; LEER
- `nullable` / kill:SELECTION — `SELECT name FROM people WHERE age > 30` → HTTP 0; LEER
- `nullable` / type:ALL — `SELECT name FROM people WHERE age > 30` → HTTP 0; LEER
- `nullable` / custom-mutant — `SELECT name FROM people WHERE age > 30` → HTTP 0
- `nullable` / type:SELECTION — `SELECT name FROM people WHERE city = 'Berlin'` → HTTP 0; LEER
- `nullable` / type:EQUIVALENCE — `SELECT name FROM people WHERE city = 'Berlin'` → HTTP 0; LEER
- `nullable` / type:NONEQUIJOIN — `SELECT name FROM people WHERE city = 'Berlin'` → HTTP 0; LEER
- `nullable` / type:AGG — `SELECT name FROM people WHERE city = 'Berlin'` → HTTP 0; LEER
- `nullable` / type:DISTINCT — `SELECT name FROM people WHERE city = 'Berlin'` → HTTP 0; LEER
- `nullable` / type:EXTRAGROUPBY — `SELECT name FROM people WHERE city = 'Berlin'` → HTTP 0; LEER
- `nullable` / type:HAVING — `SELECT name FROM people WHERE city = 'Berlin'` → HTTP 0; LEER
- `nullable` / kill:SELECTION — `SELECT name FROM people WHERE city = 'Berlin'` → HTTP 0; LEER
- `nullable` / type:ALL — `SELECT name FROM people WHERE city = 'Berlin'` → HTTP 0; LEER
- `nullable` / custom-mutant — `SELECT name FROM people WHERE city = 'Berlin'` → HTTP 0
- `nullable` / type:SELECTION — `SELECT name FROM people WHERE score > 50 AND age < 40` → HTTP 0; LEER
- `nullable` / type:EQUIVALENCE — `SELECT name FROM people WHERE score > 50 AND age < 40` → HTTP 0; LEER
- `nullable` / type:NONEQUIJOIN — `SELECT name FROM people WHERE score > 50 AND age < 40` → HTTP 0; LEER
- `nullable` / type:AGG — `SELECT name FROM people WHERE score > 50 AND age < 40` → HTTP 0; LEER
- `nullable` / type:DISTINCT — `SELECT name FROM people WHERE score > 50 AND age < 40` → HTTP 0; LEER
- `nullable` / type:EXTRAGROUPBY — `SELECT name FROM people WHERE score > 50 AND age < 40` → HTTP 0; LEER
- `nullable` / type:HAVING — `SELECT name FROM people WHERE score > 50 AND age < 40` → HTTP 0; LEER
- `nullable` / kill:SELECTION — `SELECT name FROM people WHERE score > 50 AND age < 40` → HTTP 0; LEER
- `nullable` / type:ALL — `SELECT name FROM people WHERE score > 50 AND age < 40` → HTTP 0; LEER
- `nullable` / custom-mutant — `SELECT name FROM people WHERE score > 50 AND age < 40` → HTTP 0
- `wide` / type:SELECTION — `SELECT id FROM wide WHERE a > 5 AND f > 5` → HTTP 0; LEER
- `wide` / type:EQUIVALENCE — `SELECT id FROM wide WHERE a > 5 AND f > 5` → HTTP 0; LEER
- `wide` / type:NONEQUIJOIN — `SELECT id FROM wide WHERE a > 5 AND f > 5` → HTTP 0; LEER
- `wide` / type:AGG — `SELECT id FROM wide WHERE a > 5 AND f > 5` → HTTP 0; LEER
- `wide` / type:DISTINCT — `SELECT id FROM wide WHERE a > 5 AND f > 5` → HTTP 0; LEER
- `wide` / type:EXTRAGROUPBY — `SELECT id FROM wide WHERE a > 5 AND f > 5` → HTTP 0; LEER
- `wide` / type:HAVING — `SELECT id FROM wide WHERE a > 5 AND f > 5` → HTTP 0; LEER
- `wide` / kill:SELECTION — `SELECT id FROM wide WHERE a > 5 AND f > 5` → HTTP 0; LEER
- `wide` / type:ALL — `SELECT id FROM wide WHERE a > 5 AND f > 5` → HTTP 0; LEER
- `wide` / custom-mutant — `SELECT id FROM wide WHERE a > 5 AND f > 5` → HTTP 0
- `wide` / type:SELECTION — `SELECT d, COUNT(*) FROM wide GROUP BY d` → HTTP 0; LEER
- `wide` / type:EQUIVALENCE — `SELECT d, COUNT(*) FROM wide GROUP BY d` → HTTP 0; LEER
- `wide` / type:NONEQUIJOIN — `SELECT d, COUNT(*) FROM wide GROUP BY d` → HTTP 0; LEER
- `wide` / type:AGG — `SELECT d, COUNT(*) FROM wide GROUP BY d` → HTTP 0; LEER
- `wide` / type:DISTINCT — `SELECT d, COUNT(*) FROM wide GROUP BY d` → HTTP 0; LEER
- `wide` / type:EXTRAGROUPBY — `SELECT d, COUNT(*) FROM wide GROUP BY d` → HTTP 0; LEER
- `wide` / type:HAVING — `SELECT d, COUNT(*) FROM wide GROUP BY d` → HTTP 0; LEER
- `wide` / kill:AGG — `SELECT d, COUNT(*) FROM wide GROUP BY d` → HTTP 0; LEER
- `wide` / type:ALL — `SELECT d, COUNT(*) FROM wide GROUP BY d` → HTTP 0; LEER
- `wide` / custom-mutant — `SELECT d, COUNT(*) FROM wide GROUP BY d` → HTTP 0

## custom-mutant (mutantQuery honored?)

- `single` `SELECT id FROM nums WHERE val > 20` → mutantQuery changed output
- `single` `SELECT id FROM nums WHERE val > 20 AND label = 'x'` → mutantQuery IGNORED (identical output)
- `single` `SELECT DISTINCT label FROM nums` → mutantQuery IGNORED (identical output)
- `uni` `SELECT name FROM students WHERE age > 22` → mutantQuery IGNORED (identical output)
- `uni` `SELECT s.name FROM students s JOIN enroll e ON s.id = e.sid` → mutantQuery IGNORED (identical output)
- `uni` `SELECT major, COUNT(*) FROM students GROUP BY major` → mutantQuery IGNORED (identical output)
- `uni` `SELECT major FROM students GROUP BY major HAVING COUNT(*) > ` → mutantQuery IGNORED (identical output)
- `types` `SELECT id FROM mix WHERE amount > 100.00` → mutantQuery IGNORED (identical output)
- `types` `SELECT id FROM mix WHERE active = true` → mutantQuery IGNORED (identical output)
- `types` `SELECT name FROM mix WHERE created > DATE '2020-01-01'` → mutantQuery IGNORED (identical output)
- `emp` `SELECT e.ename FROM emp e JOIN dept d ON e.did = d.did WHERE` → mutantQuery IGNORED (identical output)
- `emp` `SELECT did, AVG(salary) FROM emp GROUP BY did` → mutantQuery IGNORED (identical output)
- `emp` `SELECT did, COUNT(*) FROM emp GROUP BY did HAVING COUNT(*) >` → mutantQuery IGNORED (identical output)
- `nullable` `SELECT name FROM people WHERE age > 30` → mutantQuery IGNORED (identical output)
- `nullable` `SELECT name FROM people WHERE city = 'Berlin'` → mutantQuery IGNORED (identical output)
- `nullable` `SELECT name FROM people WHERE score > 50 AND age < 40` → mutantQuery IGNORED (identical output)
- `wide` `SELECT id FROM wide WHERE a > 5 AND f > 5` → mutantQuery IGNORED (identical output)
- `wide` `SELECT d, COUNT(*) FROM wide GROUP BY d` → mutantQuery IGNORED (identical output)

---

## UI-Test (Chrome DevTools, Port 80)

| # | Szenario | Ergebnis |
|---|----------|----------|
| 1 | Universitaet DB, `SELECT * FROM students WHERE age > 20`, Typen Selection/Equi/Aggregation | ✅ „Successfully generated 3 insert statements" + Kopieren/Download. Happy Path funktioniert. |
| 2 | `dsp_uni`, JOIN `students s JOIN enroll e ON s.id=e.sid` | ⚠️ „Successfully generated 2 inserts" — **aber semantisch kaputt**: `ENROLL(sid=5)` ohne passenden `STUDENTS`-Datensatz (nur id=3) → JOIN matcht nie → Referenz leer. **Irreführender Erfolg.** |
| 3 | „Eigene Mutante testen" aktivieren | ✅ Zweiter Editor „Eigene Mutanten-Abfrage" erscheint — UI funktioniert. ❌ Backend ignoriert `mutantQuery` jedoch komplett (No-op, siehe oben). |
| 4 | `dsp_uni`, Subquery `... WHERE id IN (SELECT sid FROM enroll ...)` | ❌ „Es konnten keine Testdaten generiert werden. Die automatische SMT-Datengenerierung ist derzeit nicht verfügbar (siehe docs/dataset-playground-analysis.md)." — entwicklerseitige, **irreführende** Meldung; `success=true` trotz Null-Ergebnis. |

## Fazit — ist das Feature „bulletproof"?

**Nein.** Die Generierung funktioniert nur für einfache Single-Table-`WHERE`-Queries zuverlässig. Hauptbefunde:

1. **Viele 500er** (≈107/181 Fälle) bei komplexeren Queries (Joins, GROUP BY/HAVING, Subqueries, diverse Typen) — Engine wirft, Controller gibt 500.
2. **Ungültige INSERTs**: Primärschlüssel-Duplikate (`Key (id)=(4) already exists`) und NUMERIC-Precision-Verletzungen → Daten laden nicht.
3. **Non-Empty-Ziel verfehlt**: Nur 16/46 ladbare Datensätze liefern für die Referenz-Query ≥1 Zeile; bei Joins fehlen FK-konsistente Zeilen.
4. **Schwache Kill-Kraft**: Nur 1/4 konstruierte Mutanten wurden durch die Daten unterschieden.
5. **„Eigene Mutante" ist ein No-op**: `mutantQuery` wird im Engine-Pfad ignoriert (14/14 identische Ausgaben).
6. **Degradation über viele Läufe**: Stabilitätstest am Ende 0/5; nach Backend-Neustart wieder funktionsfähig → statischer Engine-/Z3-State wird nicht vollständig zurückgesetzt.
7. **Irreführende UX**: Null-Ergebnisse werden mit `success=true` und einer auf interne Docs verweisenden Meldung präsentiert; semantisch nutzlose Daten als „Erfolg".

**Empfohlene Fix-Richtungen** (für eine spätere Runde): FK-konsistente & PK-eindeutige Datengenerierung; Engine-Exceptions als saubere 4xx mit klarer Meldung statt 500; `mutantQuery` tatsächlich verdrahten; Engine-State pro Lauf vollständig isolieren (kein Cross-Run-Drift); UI: ehrliche Empty-/Error-States ohne internen Doc-Verweis, `success=false` bei 0 INSERTs.

_Screenshots: `dsp-ui-01-success.png`, `dsp-ui-02-empty-msg.png`. Harness: `dsp_stress_test.py`._

---

## Behobene Schicht (UX & Robustheit, 2026-06-20)

Adressiert (UX/Robustheit, ohne tiefen Engine-Umbau):
- **Keine 500er mehr:** Engine-Fehler/Null-Ergebnis → **HTTP 422** mit klarer, nutzerseitiger Meldung statt 500 / irreführendem `success=true`. (`DatasetPlaygroundController`)
- **Ehrliche Meldungen:** „Für diese Abfrage konnten keine Testdaten erzeugt werden. Komplexe Strukturen … werden derzeit nur eingeschränkt unterstützt." — kein interner Doc-Verweis mehr.
- **Kein Hänger mehr:** `runEngine` nutzt `tryLock(60s)`; eine blockierte Generierung legt nicht mehr das ganze Feature lahm — Folge-Requests erhalten „Engine ausgelastet". (`DatasetGenerationService`)
- **Ehrliche UI:** Empty/Fehler als amber Hinweis (kein grüner Erfolg); `success` nur bei echten INSERTs; Custom-Mutant als **„(experimentell)"** gekennzeichnet + Hinweis „wird derzeit noch nicht in die Generierung einbezogen". (`DatasetPlayground.tsx`)
- **Live verifiziert:** Subquery → 422 + ehrliche Meldung; `age>22` → „3 Testdaten-Zeile(n) generiert" (grün). Screenshots `dsp-fix-01-honest-empty.png`, `dsp-fix-02-success-and-note.png`.

**Noch offen (tiefes Engine-Folgeprojekt, NICHT behoben):** PK-eindeutige & FK-konsistente Datengenerierung, NUMERIC-Precision, zuverlässiges Non-Empty-Ziel & Kill-Kraft bei Joins/GROUP BY/Subqueries, echte Verdrahtung von `mutantQuery`, vollständige Isolation des statischen Z3-Engine-State (Cross-Run-Drift).
