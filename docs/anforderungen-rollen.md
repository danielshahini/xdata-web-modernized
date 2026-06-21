# XData – Anforderungen & gewünschte Features je Rolle

> Erstellt 2026-06-21 durch end-to-end Browser-Test (chrome-devtools MCP) nach Docker-Neustart.
> Admin-Sicht vom Hauptagenten, Lehrer-/Schüler-Sicht von einem Test-Subagenten.
> Test-Accounts: `admin1/admin1` (ADMIN), `lehrer1/lehrer123` (INSTRUCTOR), `schueler1/schueler1` (STUDENT).
> Beim Test angelegt (Testdaten, bleiben in der DB): Assignment id 3 „Woche 2: WHERE & Filter (Test)" mit 2 Fragen.

---

## Gesamtbild
Die **Kernflüsse funktionieren**: Benutzer-/Kursverwaltung, Aufgaben- und Fragen-Erstellung (persistiert korrekt),
Schüler-Lösung mit **Run-Vorschau**, **asynchroner Auto-Bewertung über WebSocket**, **XP/Streak/Leaderboard/Badges**,
**progressive Hinweise** und **Cheat-Sheet**. Keine Konsolenfehler in allen drei Sessions.
Die wichtigste funktionale Lücke zieht sich durch alle Rollen: **es gibt keine UI, um einer Aufgabe ein
Standard-Schema zuzuweisen**, weshalb der (vorhandene, ER-fähige) Schema-Explorer beim Schüler leer bleibt.

---

## 1. ADMIN

**Funktioniert (verifiziert):**
- Benutzerverwaltung: Anlegen (Name, Login-ID, E-Mail, Passwort, Rolle, Kurszuweisung), CSV-Import + Vorlage,
  Suche, Sortierung, Liste mit Aktionen: deaktivieren, **impersonieren** („Als dieser Benutzer anmelden"),
  bearbeiten, Passwort zurücksetzen, löschen.
- Kursverwaltung (Tab „Kurse"): Kurs anlegen (Name + Kurs-ID), Liste, löschen.

**Gewünschte Features (priorisiert):**
1. **Audit-Log-Viewer im UI einbinden.** Der Untertitel verspricht „Verwaltung & **Monitoring**", aber es gibt
   keinen Monitoring-View. Komponente `AuditLogViewer` und ADMIN-only-Endpoint `/admin/audit-logs` existieren,
   sind aber nicht verlinkt → als dritten Tab „Audit-Log" ergänzen.
2. **System-/Analytics-Übersicht** (Dashboard-Kacheln): Gesamtzahl Nutzer je Rolle, Kurse, Aufgaben, Abgaben,
   aktive Nutzer, Bewertungs-Durchsatz.
3. **Kurs-Bearbeitung statt nur Anlegen/Löschen.** Das Course-Modell hat `year/semester/description`, das Formular
   bietet nur Name + ID. Ergänzen: Bearbeiten, Beschreibung/Semester/Jahr, **Dozenten-Zuordnung pro Kurs**.
4. **Sicherheitsabfrage bei Kurslöschung** mit Abhängigkeitsprüfung (warnen, wenn Aufgaben/Abgaben hängen).
5. **Bulk-Aktionen** (mehrere Nutzer gleichzeitig deaktivieren / Kurs zuweisen / importieren-und-zuweisen).
6. **Passwort-Policy- & Sperr-Konfiguration** im UI (min. Länge, Sperrdauer nach Fehlversuchen sind aktuell fix).

---

## 2. LEHRER (INSTRUCTOR)

**Funktioniert (verifiziert):**
- Instruktor-Panel mit Sektionen: Aufgaben, Notenbuch, Anfechtungen, Studenten, Ankündigungen, Statistiken,
  sowie „ERWEITERT": Kursinhalte, SQL-Schemas, Datenbanken, Kurse.
- **Aufgaben-Wizard (3 Schritte):** Basis-Konfiguration (Name, Deadline, Ziel-DB, Veröffentlichung, Penalty,
  Max. Versuche, Noten-Freigabe, Sichtbarkeit) → Fragenkatalog (Titel, Punkte, Themen-Tags, progressive Hinweise,
  Musterlösung-SQL, Teilbewertungs-Gewichte) → „Alle speichern". **Persistenz verifiziert** (Fragen mit Tags,
  Hints, Marks, `partialMarkParameters`).

**Bugs:**
- **[Mittel] Kein „Standard-Schema"-Feld im Wizard.** Es gibt nur „Ziel-Datenbank" (Connection), aber keinen
  Schema-Picker → `defaultSchemaId` bleibt null → Schema-Explorer beim Schüler tot.
- **[Niedrig] Keine Erfolgs-Meldung nach „Alle speichern".** Persistenz passiert, aber ohne Toast → unklar, ob
  gespeichert wurde (genau die bekannte Verwirrung „Alle speichern" vs. „Fertigstellen & Speichern").
- **[Niedrig] Schwierigkeitsgrad nicht setzbar** (siehe Schüler – wird auto-abgeleitet, oft fälschlich „HARD").

**Gewünschte Features (priorisiert):**
1. **Standard-Schema je Aufgabe wählbar** (z. B. „Universitaet DB") — damit Schüler Schema-Explorer + Autocomplete
   bekommen. Größter Hebel, geringer Aufwand (`defaultSchemaId` wird bereits durchgereicht).
2. **Schwierigkeitsgrad pro Frage explizit setzen** (Easy/Medium/Hard) statt Auto-„HARD".
3. **Klares Speicher-Feedback** (Erfolgs-Toast) und die zwei Speicher-Pfade vereinheitlichen/erklären.
4. **Musterlösung live validieren/ausführen** gegen die gewählte DB, bevor veröffentlicht wird (Syntax/Plausibilität).
5. **Vorschau „so sieht der Schüler die Aufgabe"** (inkl. Schema und erwartetem Ergebnis).
6. **Teilbewertungs-Gewichte prominenter** machen (das „Gewichte anpassen" ist tief vergraben).

---

## 3. SCHÜLER (STUDENT)

**Funktioniert (verifiziert):**
- Dashboard: Level/XP-Bar, **Streak + Tagesziel**, **Score/Gelöst/Module**-Kacheln, **Rangliste** (eigener Rang +
  Perzentil), **Abzeichen (3/6)**, Aufgabenliste mit Status-Icons & Schwierigkeits-Pills.
- Lösungs-View: **Run-Vorschau** (Ergebnis-Tabelle), **Submit + asynchrone Auto-Bewertung** (100 % bei korrekter
  Lösung, detaillierte Komponenten-Aufschlüsselung deine Lösung vs. Musterlösung), **progressive Hinweise**,
  **Cheat-Sheet**, **„Bewertung anfechten"** (Regrade).
- **Live-Updates nach Bewertung:** XP 2200→2300, Streak 1→2, Tagesziel 1/3, Score 33→67 %, Gelöst 1→2,
  Rangliste & Aufgaben-Fortschritt aktualisiert.

**Bugs:**
- **[Mittel] Schema-Explorer zeigt nichts** (kein Netzwerk-Request) — Folge des fehlenden `defaultSchemaId`.
  Schüler müssen Tabellen-/Spaltennamen raten.
- **[Niedrig] Angezeigte Schwierigkeit irreführend** („HARD" auf trivialem `COUNT(*)`).
- **[Niedrig/UX] Run-Vorschau bleibt nach Bewertung stehen** — „Vorschau" und „Letztes Ergebnis" klarer trennen.

**Gewünschte Features (priorisiert):**
1. **Schema-Explorer befüllen** (Tabellen/Spalten/PK/FK des Aufgaben-Schemas) — die mit Abstand größte Lücke.
2. **In-Editor-Autocomplete** an dieses Schema gebunden (der Editor wirbt mit `aria-autocomplete`, hat aber nichts
   zu vervollständigen).
3. **Ergebnis-Diff bei (teilweise) falschen Antworten** — erwartet-vs-tatsächlich-Zeilen, nicht nur die
   Komponenten-Aufschlüsselung; als Lernhilfe.
4. **Schwierigkeit an echte Signale koppeln** (Erfolgsquote/Komplexität) statt fixem „HARD".
5. **Versuchszähler & sichtbarer „wird bewertet…"-Zustand** während der asynchronen Bewertung (Max-Versuche zeigen).
6. **Vorschau vs. letztes Ergebnis** visuell trennen.

---

## Querschnitt: Top-Empfehlungen (rollenübergreifend)
1. **Standard-Schema-Zuweisung end-to-end** (Lehrer setzt → Schüler-Explorer + Autocomplete leben). 1 Feld, großer Effekt.
2. **Schwierigkeitsgrad teacher-gesteuert** statt Auto-„HARD".
3. **Admin-Monitoring sichtbar machen** (Audit-Log-Tab + System-Übersicht).
4. **Speicher-/Bewertungs-Feedback** durchgängig (Toasts, „wird bewertet…", Vorschau/Ergebnis trennen).
5. **Ergebnis-Diff** für Lerneffekt bei falschen Lösungen.
</content>
