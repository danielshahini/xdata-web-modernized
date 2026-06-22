# QA-Durchlauf: UI + Backend (2026-06-20)

Systematischer Bug-Test nach dem Redesign/Hardening. Methodik: API-Matrix (24 Endpoints × 4 Rollen),
Edge-Case-/Validierungs-Probe (19 Fälle), UI-Walkthrough (Student/Instructor/Admin) via Chrome DevTools
(Console- + Netzwerk-Fehler). Skripte: `api_bug_scan.py`, `edge_scan.py`.

## Gefundene & behobene Bugs (7)

| # | Schwere | Bug | Ursache | Fix |
|---|---------|-----|---------|-----|
| 1 | 🔴 Security | Musterlösung (`instructorQuery`) leakt an Studenten über `GET /student/assignments/{id}/questions` | Endpoint gab rohe `Question`-Entität zurück | Sanitisierte Antwort (nur `id,name,marks,assignmentId`) — `StudentController` |
| 2 | 🔴 Security | `GET /admin/questions/{id}` für Studenten erlaubt (Musterlösung) | `@PreAuthorize(... 'STUDENT')` | Auf `ADMIN,INSTRUCTOR` beschränkt — `QuestionController` |
| 3 | 🐞 500 | `GET /db-connections` & `/instructor/connections` als Admin → 500 | `LazyInitializationException` auf `course`-Proxy bei JSON-Serialisierung (OSIV aus) | `@JsonIgnore` auf `course` + `courseId` im Handler materialisieren — `DbConnection`, `DbConnectionController` |
| 4 | 🔴 500 | **Falsches Passwort beim Login → 500** | Failed-Login-Audit-INSERT in read-only-TX | `AuditService.log` = `REQUIRES_NEW` + best-effort (try/catch) |
| 5 | 🐞 500 | User anlegen mit fehlenden Feldern → 500 | Bean-Validation-Exception unbehandelt | `@ExceptionHandler` Validation → 400 — `GlobalExceptionHandler` |
| 6 | 🐞 500 | Nicht-numerische Pfad-ID → 500 | `MethodArgumentTypeMismatchException` unbehandelt | Handler → 400 |
| 7 | 🐞 500 | Submit ohne Body / Export für nicht-existentes Assignment → 500 | NPE / `NoSuchElementException` unbehandelt | Guard im Submit (→400) + `NoSuchElementException`-Handler (→404) |

## Verifikation (nach Fix)

- **Edge-Case-Re-Scan:** 19/19 Fälle liefern erwartete 4xx (login falsch→401, fehlende Felder→400,
  non-numeric→400, submit leer→400, export nicht-existent→404, …). Keine 500er mehr.
- **API-Matrix-Regression:** keine Anomalien; Permission-Boundaries korrekt (Student→403 auf
  Admin/Dozent-Endpoints, unauth→403 überall, `system/status` nur Admin).
- **UI (Chrome DevTools):** Student (Dashboard, Fragen, Grading, WebSocket verbunden), Instructor- &
  Admin-Dashboards, Dataset-Playground, Login-Fehlerpfad — alles funktionsfähig, keine JS-Fehler.
  `instructorQuery` ist in der Studenten-Antwort nicht mehr enthalten; Fragen laden weiterhin.

## Offen (niedrige Priorität)

- **a11y:** einige Formularfelder ohne zugeordnetes `label`/`id` (Monaco-Editoren, Selects) — Konsole meldet
  „No label associated with a form field". Kein Funktionsfehler.
- **Design-Hinweis:** Der Grading-Breakdown zeigt Studenten nach Abgabe eine „Musterlösung"-Spalte
  (Struktur-Feedback). Das ist formatives Feedback by-design; falls unerwünscht, separat entscheiden.
- **Dataset-Engine-Kern:** PK/FK/Precision/Kill-Kraft — siehe `dataset-playground-stress-report.md`.
