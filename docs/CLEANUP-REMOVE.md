# XData — Aufräumliste: Features entfernen / abspecken / einschränken

Stand: 2026-06-20. Features, die im Kontext einer **lokalen** Schul-Lernplattform keinen Sinn ergeben,
tot/Dummy sind, irreführen oder zu weit geöffnet sind. Verifiziert am Code.

---

## A. Sofort entfernen — tot, Dummy oder irreführend

### A1. `GET /schemas/datasets` — reine Dummy-Antwort
Gibt hartkodiert `["Dataset1","Dataset2","Dataset3"]` zurück (Kommentar: „Dummy-Implementierung").
→ **Endpoint löschen** (`SchemaController`); kein Aufrufer mit echtem Nutzen.

### A2. „Sample-Data hochladen" (Schema-Manager) — tut nichts
`POST /schemas/sample-data/upload` liest die Datei und **verwirft sie** („Hier würde normalerweise ein
Service aufgerufen…"). Meldet trotzdem „uploaded". Das UI (`SchemaManager.tsx`: `sampleFile`,
`uploadSampleData`, Upload-Feld) gaukelt eine Funktion vor.
→ **UI-Block + Endpoint entfernen** (oder echt implementieren). Aktuell irreführend.

### A3. „Eigene Mutante testen" (Dataset-Playground) — No-op
`mutantQuery` wird im Engine-Pfad ignoriert (in 14/14 Tests identische Ausgabe). Bereits als
„experimentell" markiert.
→ **Bedienelement entfernen**, bis es wirklich verdrahtet ist.

### A4. E-Mail-Flow „Passwort vergessen" — lokal funktionslos
`MailService` versendet nichts („Real email sending is currently disabled"). `POST /auth/forgot-password`
+ `POST /auth/reset-password` + der „Vergessen?"-Button im Login führen ins Leere (kein Mailserver auf
dem Schulserver).
→ **Entfernen** und durch **In-App-Reset** ersetzen (Admin/Dozent erzeugt Einmal-Code, oder Erst-Login-
Passwortwechsel). Siehe Roadmap P0-4.

### A5. Swagger/OpenAPI öffentlich (`permitAll`)
`/v3/api-docs/**`, `/swagger-ui/**` sind ohne Auth erreichbar → die komplette API-Oberfläche liegt offen.
→ In Produktion **deaktivieren** oder hinter Admin-Auth legen.

---

## B. Abspecken / im Kontext sinnlos

### B1. Admin „System"-Dashboard (CPU/RAM/Disk/Uptime) — *dein Beispiel*
Server-Monitoring (Heap, OS-Load, Disk-GB, Uptime) gehört nicht in eine Lern-App; das macht der
Schul-Sysadmin mit OS-Tools. Es bläht die Admin-Zentrale auf und suggeriert „Ops-Konsole".
→ **Ressourcen-Karten entfernen.** Höchstens eine schlanke Funktions-Diagnose behalten
(„DB erreichbar", „Z3 verfügbar") — der Rest raus.

### B2. „SQL-Labor" (`/playground`) für **Schüler** sichtbar
SqlLab ist ein Diagnose-Werkzeug für **Partial-Marking** (vergleicht „Musterlösung" gegen „Studentische
Abfrage"). Für Schüler ergibt das keinen Sinn (sie müssten selbst eine Musterlösung eingeben) und kann
verwirren. Nav-Link ist aktuell `show: true` für alle.
→ **Auf Dozent/Admin beschränken** (Nav + Route-Guard).

### B3. Dataset-Playground insgesamt (Dozenten-Werkzeug) — unzuverlässig
Laut Härtetest scheitert die Generierung bei den meisten komplexeren Queries (Joins/GROUP BY/Subqueries)
und liefert oft semantisch unbrauchbare Daten. Als Dozenten-Feature stiftet es mehr Verwirrung als Nutzen.
→ **Erwägen: aus dem Dozenten-UI nehmen** und nur intern für die Grading-Testdaten-Stufe nutzen, bis der
Engine-Kern robust ist (siehe `dataset-playground-stress-report.md`). Mindestens: ehrliche
Erwartungshaltung / „Beta".

---

## C. Einschränken — zu weit geöffnet (Rechte/Scope)

### C1. Audit-Logs für **Dozenten** sichtbar
`/api/v1/admin/audit-logs/**` erlaubt `ADMIN, INSTRUCTOR`. Audit-Logs enthalten systemweite
Sicherheitsereignisse (Login-Fehlschläge, Passwort-Resets, Impersonationen, alle Nutzer-CRUDs) — das ist
nichts für Dozenten (Datenschutz). Das UI blendet den Tab zwar via `isAdmin` aus, der Endpoint bleibt offen.
→ **Auf `ADMIN` beschränken.**

### C2. Impersonation per API auch für Dozenten erreichbar
`/admin/users/{id}/impersonate` liegt unter `/admin/users/**` (`ADMIN, INSTRUCTOR`). Das UI zeigt den
Button nur Admins, aber der Endpoint ist für Dozenten aufrufbar. „Als Schüler anmelden" ist heikel.
→ **Backend-seitig auf `ADMIN` beschränken** (oder ganz entfernen, falls nicht gebraucht).

---

## D. Zur Entscheidung — umstritten (nicht eindeutig „weg", aber hinterfragen)

### D1. XP/Level-Gamification
LeetCode-Stil XP/Level neben echten Schulnoten kann verwirren; Lehrkräfte interessiert die Note, nicht XP.
→ Entweder **bewusst als Motivations-Feature behalten** (dann Leaderboard/Badges ausbauen, Roadmap P2-17)
oder **entfernen/abschaltbar machen**, wenn es im Notenkontext stört.

### D2. Plagiatserkennung via Levenshtein
Text-Levenshtein auf SQL ist ein schwaches Signal: triviale Umformatierung umgeht es, legitime korrekte
Lösungen sehen sich zwangsläufig ähnlich (→ Fehlalarme). Als „Beweis" gefährlich.
→ Entweder **deutlich als grober Hinweis labeln** + Side-by-Side-Diff (Roadmap P1-13) oder **entfernen**,
bis es belastbar ist.

### D3. Zwei Playgrounds mit verwirrenden Namen
„SQL-Labor" (`/playground`) und „Dataset-Playground" (`/dataset-playground`) stehen unverbunden
nebeneinander.
→ **Zusammenlegen/umbenennen** oder (siehe B2/B3) reduzieren.

### D4. „Musterlösung"-Spalte im Schüler-Feedback
Der Grading-Breakdown zeigt Schülern nach Abgabe die Struktur der Musterlösung.
→ Als formatives Feedback evtl. gewollt — sonst **erst nach Deadline** freigeben oder weglassen.

---

## Empfohlene Sofort-Maßnahmen (klein, klar)
1. A1 + A2 entfernen (toter/funktionsloser Code).
2. A5 (Swagger) + C1 (Audit nur Admin) + C2 (Impersonate nur Admin) — Sicherheits-/Datenschutz-Cleanup.
3. B1 System-Ressourcen-Karten entfernen.
4. A4 E-Mail-Reset entfernen → In-App-Reset (zusammen mit Roadmap P0-4).
5. B2 SQL-Labor für Schüler ausblenden.
