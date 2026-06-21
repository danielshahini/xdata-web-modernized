# XData — Feature-Roadmap zur vollständigen Lern- & Bewertungsplattform

Stand: 2026-06-20. Ziel: Lücken zwischen der heutigen App und einer „vollkommenen" Lernplattform
zum **Verwalten von Schülern** und **Bewerten von Aufgaben** — inspiriert von Moodle, Canvas,
Gradescope und LeetCode.

**Rahmenbedingung:** Läuft **nur lokal auf dem Schulserver**. Keine externen Dienste
(kein E-Mail-Versand, kein SMS, kein Cloud-SSO/LTI, keine externen APIs). Alles, was anderswo per
E-Mail liefe, wird hier als **In-App-Lösung** umgesetzt (Benachrichtigungszentrale, In-App-Passwort-Reset).

---

## Was bereits vorhanden ist (Fundament)

Rollen (Admin/Dozent/Schüler) + JWT + Impersonation · Kurse, Aufgaben, Fragen, Abgaben ·
SQL-Grading (Text/Testdaten/SMT/DB-Ausführung) mit **Partial Marking** (14 Konstrukt-Gewichte) ·
Plagiatserkennung · XP/Level · Echtzeit-Grading (WebSocket) · Statistiken + Charts · Ankündigungen ·
Schema-Upload + Visualizer · DB-Verbindungen · Dataset-Playground (Z3) · Audit-Logs · CSV-Import/Export
von Nutzern · Dark-Mode · Monaco-Editor mit Auto-Completion.

Die Lücken unten bauen darauf auf.

---

## Aktueller Stand (2026-06-21) — was seit der Roadmap erledigt wurde

- ✅ **Sortierung der Nutzerliste** nach Erstelldatum/Name + „Erstellt"-Spalte.
- ✅ **CSV-Vorlage-Download** repariert (authentifiziert) und **Lehrer dürfen nur Studenten** anlegen
  (Formular + CSV-Import erzwingen STUDENT).
- ✅ **UX-Vereinfachung**: Dozenten-Panel auf 4 Primär-Tabs + „Erweitert"; Aufgaben-Wizard mit
  Progressive Disclosure.
- ✅ **Aufräumen**: tote/irreführende Features entfernt, Rechte verschärft (siehe `CLEANUP-REMOVE.md`).
- ✅ **Robustheit/Sicherheit** (früher): keine 500er bei Fehleingaben, kein Musterlösungs-Leak, Echtzeit-Grading.

**Damit sind die meisten Roadmap-Punkte unten noch offen.** Die definitive Restliste folgt — Priorität
P0 (zuerst) → P2.

---

## P0 — Kernlücken (für „Schüler managen + bewerten" am wichtigsten)

### 1. Notenbuch (Gradebook) — *Canvas/Gradescope*
Heute gibt es nur Statistik pro Aufgabe. Es fehlt die zentrale **Matrix Schüler × Aufgaben** pro Kurs:
- Gesamtnote pro Schüler, pro Aufgabe, pro Frage; Kurs-Durchschnitt.
- Sortier-/Filter-/Suchbar; Klick → Drilldown auf einzelne Abgabe.
- **Export des gesamten Kurs-Notenbuchs** als CSV/XLSX (heute nur Einzel-Aufgabe).
- Gewichtung von Aufgaben/Kategorien zur Kursnote; optional Notenschlüssel (Punkte→Note 1–6).

### 2. „Ausführen" vor „Einreichen" (Run vs. Submit) — *LeetCode*
Schüler sehen heute kein Ergebnis ihrer Query vor der Bewertung. Bauen:
- **Query gegen Beispieldaten ausführen** und Ergebnis-Tabelle anzeigen (ohne Bewertung).
- Erwartete vs. tatsächliche Ergebniszeilen visuell vergleichen.
- Klare SQL-Fehlermeldungen aus der Ausführung.
- (Sandbox/Read-Only-Validierung existiert bereits — fehlt das Ausführen + Resultset-Anzeige.)

### 3. In-App-Benachrichtigungszentrale — *Canvas (Ersatz für E-Mail)*
Da kein E-Mail-Versand: persistente **Glocke mit Benachrichtigungen**:
- Neue Note/Feedback, neue Ankündigung, nahende Deadline, Konto-Aktionen.
- Gelesen/Ungelesen-Status; pro Nutzer gespeichert; Echtzeit via vorhandenem WebSocket.

### 4. Eigenes Profil + Selbst-Service-Passwort — *alle Apps*
- **Profilseite**: eigenen Namen/E-Mail anzeigen, **eigenes Passwort ändern** (heute nur Admin-Reset; der „Passwort vergessen"-Flow ist E-Mail-basiert → lokal nutzlos).
- **In-App-Passwort-Reset** statt E-Mail: Admin/Dozent generiert einen einmaligen Reset-Code/Link, den der Schüler offline erhält (oder direkt-Reset beim ersten Login erzwingen).
- **Passwort beim Erst-Login ändern erzwingen** (wichtig bei CSV-Massenanlage).

### 5. Abgabe-Richtlinien pro Aufgabe — *Gradescope/Moodle*
- **Maximale Versuchszahl** pro Frage/Aufgabe (heute unbegrenzt).
- **Individuelle Deadline-Verlängerung** pro Schüler (Nachteilsausgleich/Krankheit).
- Sichtbarkeit/Freigabe von Noten steuerbar (sofort vs. nach Deadline „Noten veröffentlichen").

### 6. Login-Härtung — *Sicherheit*
- **Brute-Force-Sperre** (Konto/Quelle nach N Fehlversuchen temporär sperren). Fehlversuche werden
  bereits geloggt, aber nicht limitiert.
- Token-Ablauf/Refresh sauber im UI behandeln (automatischer Logout-Hinweis statt stiller Fehler).

---

## P1 — Stark wertsteigernd

### 7. Mehr Aufgaben-/Fragetypen — *Moodle/Canvas*
Heute nur SQL-SELECT. Ergänzen:
- **DDL/Schema-Design-Aufgaben**, Multiple-Choice, Lückentext/Kurzantwort (auto-bewertbar),
  Freitext mit manueller Bewertung.
- Pro Frage **Tags/Themen** (JOINs, GROUP BY, Subqueries …) statt nur abgeleiteter Schwierigkeit.

### 8. Regrade-/Einspruchs-Workflow — *Gradescope*
- Schüler kann zu einer bewerteten Abgabe einen **Einspruch/Kommentar** stellen.
- Dozent sieht Queue offener Einsprüche, kann Note **manuell überschreiben** (mit Begründung, im Audit-Log).

### 9. Übungsmodus + Aufgaben-Pool — *LeetCode*
- **Unbewertete Übungsaufgaben** / Problembank zum Trainieren ohne Kursbindung.
- **Progressive Hinweise** (kosten ggf. XP), **Musterlösung erst nach Deadline** freigeben.
- **Wiederholen falscher Aufgaben** / Review-Liste.

### 10. Diskussion & Feedback-Kanal (lokal) — *Moodle-Forum*
- **Q&A/Forum pro Aufgabe oder Kurs** (lokal), Dozent kann anpinnen/beantworten.
- Optional **Direktnachricht** Dozent↔Schüler (lokal, In-App).

### 11. Kursinhalte & Material — *Canvas/Moodle*
- **Lernmaterial pro Kurs** (PDF/Folien/Links/Markdown-Seiten), Syllabus/Kurs-Startseite.
- **Kalender-/Terminansicht** aller Deadlines; optional sequenzielles Freischalten von Aufgaben.
- Aufgaben in **Module/Wochen** gruppieren.

### 12. TA-/Tutor-Rolle + Sektionen — *Canvas*
- Rolle zwischen Dozent und Schüler (darf bewerten/Feedback, nicht Kurse anlegen).
- **Gruppen/Sektionen** innerhalb eines Kurses; **Admin weist Dozenten Kursen zu** (UI fehlt heute).

### 13. Erweiterte Analytik & Item-Analyse — *Gradescope*
- **Item-Analyse** je Frage (Schwierigkeit, Trennschärfe), Abgabe-Zeitverlauf, Versuchs-Histogramme.
- **Aktivitäts-Feed**/Timeline; druckbare/exportierbare Berichte.
- Plagiat: **Side-by-Side-Diff** und Cluster statt nur Liste.

### 14. Integrität: pro-Schüler-Datensätze + Prüfungsmodus — *eigene Stärke (Z3)*
- Die Killing-Data-Engine nutzen, um **pro Schüler leicht variierte Datensätze** zu generieren
  (erschwert Copy-Paste-Lösungen).
- **Prüfungs-/Timed-Modus**: Countdown, ein Versuch, Aufgaben-Reihenfolge mischen, Honor-Code-Bestätigung.

---

## P2 — Reife, Betrieb & Komfort

### 15. Backup/Restore & Kurs-Migration (lokal) — *Schulserver-Betrieb*
- **DB-Backup/Restore aus dem Admin-UI** (Dump/Restore), geplante lokale Backups.
- **Kurs exportieren/importieren** (Aufgaben+Fragen+Schemas) zur Wiederverwendung im nächsten Semester.
- Semester-/Term-Lebenszyklus: Kurse **archivieren**, alte Daten aufräumen.

### 16. Einstellungen-/Konfig-UI — *alle*
- Standard-Penalty, Grading-Policy, Notenschlüssel, Branding/Logo, Sprache zentral konfigurierbar
  (statt hartkodiert/ENV).

### 17. Gamification ausbauen — *LeetCode/Duolingo*
- **Leaderboard** (opt-in, pro Kurs), Badges/Achievements, Streaks (XP existiert bereits).
- Fortschritts-/Mastery-Anzeige pro Thema.

### 18. Schüler-Komfort
- **Entwurf speichern/Autosave** im Editor, Query-Verlauf mit Diff, Query-Formatierer.
- Bookmarks/Notizen zu Aufgaben.

### 19. Barrierefreiheit & i18n
- **a11y**: Formularfelder mit Labels/IDs (heute Konsolen-Warnungen), Fokus-/Tastaturpfade, Kontraste.
- i18n-Gerüst (heute Strings inline, nur Deutsch) — vorbereitet für EN, falls nötig.

### 20. Betriebs-Sichtbarkeit
- **Job-/Queue-Status** der asynchronen Bewertung im Admin-UI (laufend/fehlgeschlagen/Retry).
- Log-Viewer; Rate-Limit-/Health-Erweiterungen (System-Status existiert bereits).

---

## Empfohlene Reihenfolge (für „managen + bewerten")

1. **Notenbuch + Kurs-Export** (1) und **Run-vor-Submit** (2) → größter unmittelbarer Nutzen.
2. **Profil + Selbst-Service-/Erst-Login-Passwort** (4) und **Login-Härtung** (6) → Betrieb mit echten Klassen.
3. **Benachrichtigungszentrale** (3) und **Abgabe-Richtlinien/Deadline-Extensions** (5).
4. **Regrade-Workflow** (8), **mehr Fragetypen + Tags** (7), **Übungsmodus** (9).
5. **TA-Rolle/Sektionen** (12), **Kursinhalte/Kalender** (11), **erweiterte Analytik** (13).
6. **Backup/Restore + Kurs-Migration** (15), **pro-Schüler-Datensätze/Prüfungsmodus** (14), Rest P2.

## Quick Wins (klein, hoher Effekt)
- Eigenes Passwort ändern + Erst-Login-Wechsel erzwingen (Teil von 4).
- Kurs-Notenbuch-CSV-Export (Teil von 1) — Daten existieren bereits, nur Aggregation/Export.
- Brute-Force-Sperre beim Login (6) — Audit-Daten existieren bereits.
- a11y-Form-Labels (19) — bekannte Konsolen-Warnungen.
- Benachrichtigungsglocke auf Basis des vorhandenen WebSocket (3).
