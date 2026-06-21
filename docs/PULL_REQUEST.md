# PR: refactor/submission-evaluation-module → developer

**Titel:** Lern- & Bewertungsplattform: Redesign, Stabilisierung, Security & 10 neue Features

## Überblick
Dieser Branch bringt das XData-Web von „funktioniert" auf „klassentauglich": professionelles
UI-Redesign, behobene Kernbugs, Sicherheits-/Datenschutz-Härtung sowie 10 neue Lehr-/Bewertungs-Features.
Läuft vollständig lokal (kein E-Mail/externe Dienste).

## Inhalt

### UI & Stabilität
- Professionelles Frontend-Redesign (einheitliches Design-System, Dark-Mode), UX-Vereinfachung
  (Dozenten-Panel-Gruppierung, Progressive Disclosure im Aufgaben-Wizard).
- Reparatur der Kernflüsse (Echtzeit-Grading per WebSocket, Statistiken, Nutzer-Bearbeitung,
  Dataset-Playground) + Härtung (keine 500er bei Fehleingaben → saubere 4xx/404/405).
- Cleanup: tote/irreführende Features entfernt, Rechte verschärft (Audit-Logs nur Admin,
  Swagger nicht mehr öffentlich, Impersonation Admin-only).

### Sicherheit & Konten
- Brute-Force-Sperre beim Login; Selbst-Service-Passwortwechsel + erzwungener Wechsel beim Erst-Login.
- Behoben: Musterlösung (`instructorQuery`) leakte an Studierende; Lehrer können keine Admins/
  Instruktoren mehr anlegen (Formular + CSV-Import).

### Neue Features (10)
1. **Notenbuch** (Matrix Schüler × Aufgaben) + kursweiter CSV-Export
2. **„Ausführen" vor „Einreichen"** (Query gegen DB, Ergebnis-Vorschau, read-only)
4. **Profil / Selbst-Service-Passwort** (lokal, ohne E-Mail)
5. **Abgabe-Richtlinien**: max. Versuche · Noten-Freigabe · pro-Schüler-Deadline-Verlängerung
6. **Brute-Force-Sperre**
7. **Themen-Tags** an Fragen
8. **Anfechtung + manuelle Noten-Korrektur**
9. **Progressive Hinweise**
11. **Kursinhalte/Lernmaterial**
12. **TA-/Tutor-Rolle** (darf bewerten, nicht verwalten)

### Datenbank
- Flyway-Migrationen V13–V21 (Audit/Reset-ID-Fixes, Login-Security, Abgabe-Richtlinien,
  Deadline-Verlängerungen, Anfechtungen, Kursmaterial, Tags, Hinweise).

## Verifikation
- Jedes Feature per API verifiziert, viele zusätzlich live im Browser (Chrome DevTools).
- Regressions-Scans (`docs/audit/api_bug_scan.py`, `edge_scan.py`) ohne Befunde; Rollen-Boundaries
  korrekt (Student/Tutor 403 auf Verwaltungs-Endpoints, kein 500er, keine Leaks).

## Bewusste Folgeprojekte (dokumentiert)
Weitere Fragetypen (MC/DDL/Freitext), eigenständiger Übungs-Pool, Kurs-Sektionen, Kalender-Widget.
