# Wettbewerbs-/UI-Analyse: Lernplattformen ähnlich zu XData

> Erstellt 2026-06-21. Ziel: konzeptuell ähnliche SQL-/Coding-Lernplattformen identifizieren,
> ihre UI durchgehen und die wiederkehrenden Muster festhalten — als Referenz für XDatas eigene UI.

## Was XData konzeptuell ist (Einordnung)

XData ist ein **Hybrid aus drei Plattform-Familien**, die sonst meist getrennt auftreten:

1. **Interaktiver SQL-Editor mit Auto-Grading** (wie LeetCode / HackerRank / DataLemur)
2. **Gamifizierte Lern-UX** (XP, Schwierigkeitsgrade — wie Boot.dev / DataCamp)
3. **Akademisches LMS / Autograder** (Kurse, Instructor/Student/TA-Rollen, Aufgaben, Regrade,
   manuelle Notenüberschreibung, Lernmaterial — wie Gradescope / Codio / zyBooks)

Diese Dreifach-Kombination ist selten. Die nächsten echten Hybride sind **DataCamp for Classrooms**
und **Gradescope**. XDatas „Dataset Playground“ (Killing-Data-/Mutationstest-Generierung) stammt aus
der akademischen SQL-Grading-Forschung (XData / IIT Bombay) und hat bei den Mainstream-Wettbewerbern
praktisch kein Pendant.

---

## Top-Plattformen (nach konzeptueller Ähnlichkeit)

| # | Plattform | Cluster | Was XData davon teilt |
|---|-----------|---------|------------------------|
| 1 | **LeetCode (Database)** | Practice + Auto-Grade | Problem→Editor→Submit, Schwierigkeitsgrade, Ergebnis-Tabelle |
| 2 | **HackerRank (SQL)** | Practice + Auto-Grade + Edu | Editor, Dialekt, Run/Submit, „clean & intuitive“, Schul-Einsatz |
| 3 | **DataCamp** | Course + Gamification + Classrooms | Instruktion+Editor+Konsole, XP/Streaks, Kursklassen |
| 4 | **DataLemur** | Practice + Auto-Grade | SQL-Editor, Schwierigkeit, Lösung/Diskussion, reale Fragen |
| 5 | **StrataScratch** | Practice + Auto-Grade | Validierung gegen Referenzlösung, Hints, erwartete Ausgabe |
| 6 | **SQLBolt** | Tutorial (interaktiv) | Lektion→Übung→Korrekt, lineares Freischalten, Null-Friction-Start |
| 7 | **SQLZoo** | Tutorial (Wiki) | „Try it yourself“, sofortiges Feedback, Uni-Einsatz |
| 8 | **Mode SQL Tutorial** | Tutorial (Analyst) | reale Datensätze, Editor + Ergebnis |
| 9 | **Codecademy (Learn SQL)** | Course | Instruktions-Pane + Editor + Checkpoint, Fortschritt |
| 10 | **Boot.dev** | Gamification (stark) | XP, Level, Streaks, Quests, Achievements, RPG-Framing |
| 11 | **Gradescope / Codio / zyBooks** | Akademischer Autograder | Instructor/Student, Aufgaben, Autograding, Regrade, manuelle Note |
| 12 | **Exercism / freeCodeCamp** | Free + strukturiert | strukturierter Pfad, Mentoring (Exercism), kostenlos |

---

## Gemeinsame UI-Muster (Querschnitt) — und wo XData steht

### A. Layout & Struktur
1. **Zwei-/Drei-Spalten-Split**: Aufgabe/Instruktion (links) · Code-Editor (Mitte) · Ergebnis/Output
   (unten oder rechts). Quasi universell (LeetCode, HackerRank, DataLemur, DataCamp, Codecademy,
   StrataScratch). → *XData: SQL-Labor hat Editor + Ergebnis; Instruktions-Pane weniger ausgeprägt.*
2. **Vollwertiger Code-Editor** mit Syntax-Highlighting, Autocomplete, Zeilennummern, Dark-Theme —
   meist Monaco oder CodeMirror. → *XData nutzt **Monaco** + schema-getriebenes Autocomplete (gleiche Liga).* 
3. **Persistente Top-Navbar**: Logo, Hauptbereiche, User/Profil, Theme-Toggle. → *XData hat genau das
   (Logo „XData/sql“, Dashboard, SQL-Labor, Dataset-Playground, Theme-Toggle, Rollen-Badge, Logout).* 
4. **Schema-/Tabellen-Referenz sichtbar**: Schema-Explorer mit Tabellen/Spalten, oft ER-Diagramm. →
   *XData: Schema-**Auswahl** vorhanden + Metadaten fürs Autocomplete; ein sichtbarer Schema-/ER-Explorer fehlt noch.*
5. **Run vs. Submit getrennt**: „Run“ zum Testen, „Submit“ zum Bewerten (HackerRank, LeetCode, DataCamp).
   → *Mögliche XData-Lücke: klare Trennung Probelauf ↔ Abgabe/Bewertung.*

### B. Feedback & Grading
6. **Sofortiges Korrektheits-Feedback** (grüner Haken / rot), oft **erwartet-vs.-tatsächlich-Diff**.
7. **Ergebnis als Datentabelle** gerendert.
8. **Progressive Hints** (StrataScratch; XData hat „progressive hints per question“ — Batch 4 #9). ✔
9. **Schwierigkeits-Badges** (Easy/Medium/Hard), farbcodiert. → *XData zeigt **EASY/MEDIUM/HARD**.* ✔
10. **Lösungs-/Diskussions-Tabs** (LeetCode, DataLemur). → *XData-Lücke: keine Community-Lösung/Diskussion.*

### C. Gamification & Motivation
11. **XP-Punkte und Level** (Boot.dev, DataCamp). → *XData hat **XP** (Echtzeit-Update via WebSocket).* ✔
12. **Streaks / Tagesziele** (Boot.dev, DataCamp, Duolingo-Modell). → *XData-Lücke: keine Streaks/Daily-Goals.*
13. **Badges/Achievements** (Boot.dev). → *XData-Lücke.*
14. **Fortschrittsbalken / Completion-%, Lern-Pfade/Maps** mit Freischalten. → *teilweise; ausbaubar.*
15. **Leaderboards** (HackerRank). → *XData-Lücke: kein Kohorten-/Kurs-Leaderboard.*

### D. Onboarding & Ästhetik
16. **Reibungsloser Start** (SQLBolt: kein Signup nötig, in <30 s erste Query).
17. **Clean, minimal, content-first**; **Dark-Mode** für Editoren Standard. → *XData: Dark-Mode-Toggle vorhanden.* ✔
18. **Marketing-Landing mit Code-Snippet-Hero**. → *XData-Login hat genau das (`auth.sql`-Snippet,
    EASY/MEDIUM/HARD, „SQL lernen, Query für Query“).* ✔
19. **Lineare Lektions-Progression** mit Unlocking (SQLBolt, Boot.dev, Codecademy).

### E. Kurs-/LMS-Dimension (der Autograder-Cluster — am nächsten an XData)
20. **Rollengetrennte Sichten**: Student vs. Instructor/Admin (+ TA). → *XData: ADMIN/INSTRUCTOR/STUDENT/**TUTOR** Dashboards.* ✔
21. **Aufgaben/Problem-Sets** mit Kurs-Kontext. → *XData: Assignments je Kurs.* ✔
22. **Autograding mit manueller Überschreibung / Regrade-Anfragen** (Gradescope). → *XData hat **Regrade
    + manuelle Notenüberschreibung** (Batch 2 #8).* ✔
23. **Topic-/Themen-Tags** zur Kategorisierung. → *XData: „topic tags for questions“ (Batch 4 #7).* ✔
24. **Lernmaterial / Referenzinhalte** neben der Praxis. → *XData: „course learning materials“ (Batch 3 #11).* ✔
25. **Echtzeit-Grading-Feedback**. → *XData: Live-XP/Bewertung über WebSocket.* ✔

---

## Verdichtung: Wo XData stark ist vs. wo Lücken sind

**Stark / auf Augenhöhe (✔):** Monaco-Editor, Top-Navbar, Dark-Mode, Code-Snippet-Landing,
Schwierigkeits-Badges, XP (sogar Echtzeit), progressive Hints, Topic-Tags, Lernmaterial,
rollenbasierte Dashboards inkl. TA, Autograding mit Regrade + manueller Note. Der **akademische
Autograder-Cluster ist XDatas Kernstärke** — hier ist es vollständiger als reine Practice-Sites.

**Häufige Muster, die XData (noch) fehlen — priorisierte Kandidaten:**
- **Run ↔ Submit klar trennen** (Probelauf ohne Bewertung).  ‹Layout #5›
- **Erwartet-vs-tatsächlich-Diff** bei falscher Lösung statt nur Pass/Fail.  ‹Feedback #6›
- **Sichtbarer Schema-/ER-Explorer** (Tabellen/Spalten/Beziehungen) neben dem Editor.  ‹Layout #4›
- **Streaks / Tagesziele** und **Badges/Achievements** zur Retention.  ‹Gamification #12,13›
- **Kurs-/Kohorten-Leaderboard**.  ‹Gamification #15›
- **Fortschritts-/Lernpfad-Visualisierung** mit Unlocking.  ‹Gamification #14›
- (Optional) **Diskussions-/Musterlösungs-Tab** pro Aufgabe.  ‹Feedback #10›

---

## Quellen
- SQLBolt — https://sqlbolt.com/
- SQLZoo — https://sqlzoo.net/wiki/SQL_Tutorial
- StrataScratch, „A Comprehensive Review of Online Platforms for SQL Practice“ — https://www.stratascratch.com/blog/a-comprehensive-review-of-online-platforms-for-sql-practice
- DataLemur — https://datalemur.com/
- SQL Practice Sites Compared (2026) — https://sqlquest.app/sql-practice-comparison/
- Boot.dev (Dashboard/Gamification) — https://www.boot.dev/ · https://www.boot.dev/dashboard
- Mode SQL Tutorial — https://mode.com/sql-tutorial/
- „12 gamification platforms that help learn coding“ — https://medium.com/@tom_z_official/12-gamification-platforms-that-help-learn-coding-814aeb04341e
- Scrimba, „Best Coding Practice Platforms 2026“ — https://scrimba.com/articles/best-coding-practice-platforms-and-challenge-websites-in-2026/
</content>
</invoke>
