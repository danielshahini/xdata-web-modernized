# XData Web — Product Requirements & Audit

> **Goal:** Make XData Web look and feel like a top-tier SaaS product.
> **Method:** Full codebase analysis + live, role-by-role functional testing via Chrome DevTools against the Docker deployment (`http://localhost`, backend `:8080`).
> **Date:** 2026-06-20 · **Branch:** `refactor/submission-evaluation-module`
> **Verdict:** The core grading engine is genuinely good and works end-to-end. The surrounding app has several **broken core flows**, a **dead real-time channel**, and is **not yet SaaS-grade** on design, accessibility, SEO, and performance hygiene. This document is ordered **functionality first, design second**, as requested.

Screenshots referenced below live in `docs/audit/screenshots/`.

---

## 0. How this was tested

A real, end-to-end scenario was driven through the live UI for **all three roles**:

| User | Role | Password | Result |
|------|------|----------|--------|
| `admin1` | ADMIN | `admin1` | ✅ login OK |
| `lehrer1` (Erika Lehrer) | INSTRUCTOR | `lehrer123` | ✅ login OK |
| `schueler1` (Max Schüler) | STUDENT | `schueler1` | ✅ login OK |

Full chain exercised: create course → create users → assign to course → upload SQL schema → inspect schema metadata → create DB connection → create assignment → author a question with a reference SQL solution → student submits → automatic grading → score / XP / level update. SQL and a test schema (`docs/audit/test-schema.sql`) were generated to drive the flow.

---

## 1. FUNCTIONALITY — what works

These were **verified live**, not just read in code:

- **Auth & RBAC** — Login/logout and JWT for all three roles; role-based redirects (`/admin`, `/instructor`, `/student`); protected routes.
- **User management (create)** — Creating STUDENT/INSTRUCTOR users works; password min-length (8) is enforced with a clear message.
- **Course creation (admin)** — Works; toast confirmation.
- **Schema upload + metadata** — `.sql` DDL upload works; metadata extraction correctly parsed 3 tables with columns/types (`10-schema-metadata.png`).
- **DB connections** — Create / edit / test work; backend validates the connection before saving.
- **Assignment + question authoring** — Assignment wizard works; instructor reference SQL is validated against the schema before save (`11-instructor-assignment-created.png`).
- **Playground (SQL Diagnose Labor)** — Structural partial-marking works and renders a per-component breakdown (`04`, `05`).
- **End-to-end grading** — Student submission → async evaluation → correct mark, and **XP/level gamification works** (XP 0→100, Level 1→2, score 10/10) (`12`, `14`). *(Note: the earlier code read suggested XP was "not integrated" — that is wrong; it is live.)*
- **System monitoring (admin)** — CPU/RAM/disk/uptime + Z3 status (`03`).
- **Dark mode** — Works app-wide (one exception, see Design).

---

## 2. FUNCTIONALITY — what is BROKEN (priority order)

> This is the part that blocks "top-tier". Each item was reproduced live with the network/console evidence noted.

### P0 — Core flows broken

1. **Admin "Edit user" + course assignments not reflected.** ✅ **FIXED (2026-06-20)**
   The edit modal itself does open (the original "button does nothing" report was a snapshot-timing artifact). The real bug: course assignments were **invisible and uneditable** — the user table's KURS column showed "Kein Kurs" and the edit modal's course checkboxes were unchecked even for enrolled users, and saving then **wiped** the assignment. Root causes:
   - `/admin/users` returns each user's `courses` (objects) but no flat `courseIds`; the frontend only read `courseIds`. → Frontend now derives `courseIds` from `courses` on load (`UserManager.tsx`).
   - On save, `PUT /admin/users/{loginId}` bound the body to the `XDataUser` entity, whose derived transient `courseIds` property silently dropped the incoming ids, so every save reset the user's courses to empty. → `updateUser` now reads the JSON body as a `Map` and applies `courseIds` explicitly.
   **Verified live:** KURS column shows the course; edit modal pre-checks it; unchecking+save → "Kein Kurs", checking+save → course restored, both persisted (`19`, `21`).

2. **Instructor "Statistics" is completely empty even with graded data.** ✅ **FIXED (2026-06-20)**
   The view showed *"Keine Assignment-Daten…"* because the frontend called endpoints that don't exist — `GET /assignments/{id}/summary` and `/analytics` (500 "No static resource"). → `AssignmentStats.tsx` now aggregates the overview + per-student summary **client-side** from the working `/evaluation/submissions/{qId}` data (+ `/assignments/{id}/questions`, `/evaluation/plagiarism`), and the empty state guides the user to open stats per assignment.
   **Verified live:** overview shows Ø-points + per-question chart, the submissions/feedback tab lists all attempts, plagiarism tab works (`20`).

3. **Real-time grading is dead — results require a manual reload.** ✅ **FIXED (2026-06-20)**
   After a student submits, the result panel showed **"0% Korrekt"** and never updated; the score only appeared after a full page reload. Three independent root causes were found and fixed:
   - **`GET /ws-grading/info` returned 403** — the STOMP/SockJS handshake wasn't permitted by the security config → added `.requestMatchers("/ws-grading/**").permitAll()` in `SecurityConfiguration`.
   - **Login response had no `loginId`** — the client subscribed to `/topic/grading/undefined` while the backend published to `/topic/grading/{loginId}` → added `loginId` to `LoginResponse` + `AuthController`.
   - **Fragile `WebSocketService`** — reused a single (closed) SockJS instance and never re-subscribed after (re)connect → rewrote it to create a new SockJS per attempt and re-apply subscriptions on every `onConnect`. Added a frontend `.dockerignore` so prod builds don't reuse stale local artifacts.
   - **"grading…" state** — the per-question result panel no longer flashes a stale 0% before the push: `submitSolution` sets a `gradingQuestionId` immediately (before the request resolves) which renders a "Wird bewertet…" panel and hides the old result; the WS push (or a 12s fallback) clears it and shows the graded result.
   **Verified live:** browser receives the STOMP message and the dashboard updates **without reload** (XP live); a fresh submit goes `result → grading → result` with **no 0% flash** (`sawZeroFlash:false`).

3b. **Async grading race: "Submission not found".** ✅ **FIXED (2026-06-20)**
   The `@Transactional` `StudentController.submit` saved the submission and triggered `evaluateSubmissionAsync(id)` **inside the still-uncommitted transaction**, so the async grader (separate transaction) often couldn't see the row and failed with `RuntimeException: Submission not found` — leaving correct answers ungraded at 0%. Fixed by deferring the async trigger to `afterCommit` via `TransactionSynchronizationManager`. *Verified:* 8/8 rapid submits now grade successfully, 0 failures (previously ~50% failed under load).

4. **Question authoring has a silent data-loss trap.**
   In the assignment wizard, **"Fertigstellen & Speichern" saves the assignment metadata but NOT the questions you just typed.** Questions are only persisted by the separate **"Alle speichern"** button. Saving via the obvious "finish" button reported success while `GET /assignments/1/questions` returned `[]`. *Evidence: "1 von 1 Fragen erfolgreich gespeichert" only after "Alle speichern".*
   **Requirement:** One save that persists everything, or block "finish" with a clear warning about unsaved questions.

### P1 — Confusing / inconsistent

5. **Duplicate tab bar (instructor).** ✅ **FIXED (2026-06-20)**
   The header pill bar (`menuItems.slice(0,3)`) duplicated the first three items of the full sidebar nav. Removed the redundant header pills in `InstructorDashboard.tsx`; the sidebar is now the single navigation. *Verified live: one tab bar, switching works (`22`).*

6. **Instructor is shown a "Create course" form they're not allowed to use.** ✅ **FIXED (2026-06-20)**
   Course creation is intentionally admin-only (`createCourse` calls `requireAdmin()`), so the instructor's "Neuen Kurs erstellen" form always 403'd. Replaced it in `InstructorDashboard.tsx` with a read-only "Ihre Kurse" panel + an info note that admins assign courses. *Verified live: no create form, course list shown (`23`).*

7. **Dataset Playground generation is non-functional.** ✅ **FIXED (2026-06-20)**
   "Dataset generieren" always returned *"…SMT-Datengenerierung ist derzeit nicht verfügbar"*. The reconstructed legacy datagen engine actually ran but the Z3 stage threw `enumeration sort name is already declared` (then a cascade NPE), which `runEngine` swallowed → empty result. Root causes fixed in the legacy Z3 layer:
   - `ConstraintGenerator.putEnumSortInContext` and the column-enum declaration both re-declared a sort name already present in the Z3 context → made both **idempotent** (reuse the existing `EnumSort` from `ctxSorts`).
   - Added `ConstraintGenerator.resetContext()` (fresh `Context`/`Solver` + `intNull`/`realNull` + cleared maps) and call it per run in `DatasetGenerationService.runEngine`, so the static context no longer accumulates declarations across requests.
   **Verified live:** Dataset Playground now returns real, targeted INSERTs (e.g. boundary rows `age=21`/`age=20` for `WHERE age > 20`), with Copy/Download buttons (`24`). *Repeat runs are stable (no "already declared").*

### P2 — API / validation hygiene

8. **`assign-course` silently no-ops on a numeric ID.** ✅ **FIXED (2026-06-20)**
   `POST /admin/users/{id}/assign-course?courseId=1` returned **HTTP 200 but assigned nothing**. `assignCourse` now returns **400** when no id is given or a given id doesn't resolve to a course, instead of silently succeeding. *Verified: numeric id → 400, empty → 400, `DB1-2026` → 200.*

9. **System-DB rejected too late + wizard advanced on failed save.** ✅ **FIXED (2026-06-20)**
   - `DbConnectionController.validateConnectionData` now rejects the system DB URL at **create/update time** (400), not only when an assignment uses it. *Verified: system-DB connection → 400, other DB → 200.*
   - The assignment wizard's "Nächster Schritt" / "Fertigstellen" now `await` the save and only advance/close when it **succeeds** (`if (saved) …`) — no more advancing past a failed save. *Verified live: happy path advances to step 2.*
   - Also removed a dead (no-op) delete button from the instructor's read-only course list.

---

## 3. SECURITY (functional, must-fix)

- **Password hashes are returned by the API.** ✅ **FIXED (2026-06-20)** `GET /admin/users` etc. exposed the BCrypt `password` field. Added `@JsonProperty(access = WRITE_ONLY)` to `XDataUser.password` — it still deserializes on input (login/create) but is never serialized out. *Verified live: `password` absent from `/admin/users`, login still HTTP 200.*
- **WebSocket handshake 403** (see #3) — also a config correctness issue.
- **Weak default credentials** — `admin1/admin1` (6 chars) bypasses the 8-char rule that applies to new users. Enforce strong admin passwords; force first-login rotation.
- **No password complexity** beyond length; no rate-limiting/lockout observed on login.
- **Secrets in `docker-compose.yml`** — DB password committed in plaintext (`1709`). Move to env/secret.

---

## 4. DESIGN

The visual language is already decent (dark-first, gradient hero, consistent cards). To reach top-tier SaaS:

- **Light mode is half-baked.** The Monaco SQL editor stays **dark** on a light page — a black box in an otherwise white layout (`15-light-mode.png`). Theme the editor with the app.
- **No design system / tokens.** Tailwind is loaded from **CDN with an inline config in `index.html`**. Move to a real Tailwind build with a token scale (color, spacing, radius, shadow, typography) and a small component library (Button, Input, Modal, Table, Badge, Card, Toast) so spacing/states are consistent.
- **Empty & loading states** are mostly bare text ("KEINE … GEFUNDEN"). Design proper empty states (icon + explanation + primary action) and skeletons (a Skeleton component already exists — use it everywhere data loads).
- **Tables** (users, connections, schemas) need consistent density, hover, zebra/row-focus, and column alignment; action icons need labels/tooltips.
- **Typography & hierarchy** — heavy ALL-CAPS micro-labels everywhere reduce scannability; define a type scale and use sentence case for body.
- **Brand polish** — favicon/logo, 404 page, consistent iconography, motion that respects reduced-motion (see A11y).

---

## 5. UX

- **Destructive & async feedback** — Several actions (#1, #4) report success or nothing while failing. Every mutation needs: optimistic/disabled state → explicit success or actionable error. Replace generic "Unbekannter Fehler" with the server message (it exists — e.g. the system-DB error was clear at the API level).
- **Wizard integrity** — don't advance steps on failed saves; show unsaved-changes guards.
- **Navigation** — collapse the duplicate tab bar; mark the active tab clearly; persist the selected course across tabs (it already mostly does).
- **Forms** — inline validation before submit (e.g., password length, JDBC URL shape), and show the field that failed.
- **Onboarding gap** — a freshly admin-created instructor has **no course** and therefore can do almost nothing (empty dropdowns everywhere). Provide a guided first-run ("create or get assigned to a course") and let admins assign courses in the (currently broken) edit dialog.
- **Result clarity for students** — distinguish "submitted, grading in progress" from "graded 0%". Explain why partial credit was/wasn't given (the playground shows structure; real submissions are result-based and silently give 0 for a wrong result set — surface this difference).

---

## 6. PERFORMANCE

- **Third-party CDNs on the critical path** — Tailwind (`cdn.tailwindcss.com`), Monaco (`cdn.jsdelivr.net`), and fonts are pulled from CDNs at runtime. This breaks offline/air-gapped use, adds render-blocking round-trips, and is a supply-chain risk. Bundle them.
- **Build tooling** — app is CRA (`react-scripts`). Migrate to **Vite** for faster builds, code-splitting, and modern output; add bundle analysis.
- **Tailwind via CDN ships the entire engine** to the browser instead of a purged stylesheet — large CSS payload. Compile + purge.
- **No measured budgets** — add Lighthouse CI; target LCP < 2.5s, CLS < 0.1, TBT < 200ms. (Lighthouse can be run directly via the DevTools MCP once the above are addressed.)
- **List endpoints** return full nested objects (with password hashes) — trim payloads with DTOs; paginate user/audit lists.

---

## 7. SEO

> Lower priority for an authenticated internal tool, but required for a credible public SaaS shell (marketing/login pages).

- Static `<title>XData Web</title>`; **no per-route titles**, no `meta description`, no Open Graph/Twitter cards, no canonical, no `robots.txt`/`sitemap.xml`.
- No structured data, no favicon set, no social preview image.
- **Requirement:** Add a head manager (e.g. `react-helmet-async`); give the public/login pages real titles + descriptions + OG tags; keep authenticated app routes `noindex`.

---

## 8. ACCESSIBILITY

Currently far from compliant:

- **Modals** have no `role="dialog"`/`aria-modal`, no focus trap, no Esc-to-close, no return-focus.
- **Icon-only buttons** (theme toggle, table row actions) lack accessible names in places.
- **No skip-to-content link**, limited landmark structure.
- **Motion** — multiple `animate-*` effects with no `prefers-reduced-motion` fallback.
- **Forms** — labels exist (good), but errors aren't programmatically associated (`aria-describedby`) and live regions are inconsistent.
- **Color contrast** — verify the many gray-on-dark micro-labels meet WCAG AA.
- **Requirement:** Target **WCAG 2.1 AA**; add axe to CI; keyboard-test all dialogs and the SQL editor.

---

## 9. MISSING / INCOMPLETE FEATURES

- **Edit user** (P0 #1) — effectively missing from the UI.
- **Instructor analytics** (P0 #2) — backend endpoints `/summary`, `/analytics` not implemented; charts/plagiarism view non-functional in UI.
- **Dataset/killing-data generation** (P1 #7) — not working.
- **Email delivery** — password-reset "email" only logs a URL server-side (MailService disabled); password reset is not actually deliverable to users.
- **Course deletion** (instructor) — trash icon with no handler (per code).
- **Sample-data upload** & **default datasets** — stubbed endpoints returning placeholders.
- **LMS integration** — `xdata_lms_credentials` table exists, no controllers/services.
- **i18n** — German-only, hardcoded strings; no framework. Needed for international SaaS.
- **No global error boundary** — a render error blanks the app.
- **Test coverage** — only a couple of unit tests on the frontend; no component/e2e tests guarding these flows (which is why the above regressions ship).

---

## 10. Suggested roadmap

**Sprint 1 (make it actually work):** P0 #1–#4 (edit user, statistics endpoints, WebSocket 403, question-save trap) + remove password hashes from API + collapse duplicate tabs.
**Sprint 2 (trust & polish):** P1/P2 items, light-mode editor, empty/loading/error states, bundle Tailwind+Monaco, migrate to Vite.
**Sprint 3 (SaaS-grade):** design tokens + component library, WCAG AA pass, SEO on public pages, Lighthouse/axe in CI, i18n scaffolding, e2e tests for the three role flows.

---

## Appendix — screenshot index (`docs/audit/screenshots/`)

| File | Page |
|------|------|
| `01-admin-users.png` | Admin · Users |
| `02-admin-courses.png` | Admin · Courses |
| `03-admin-system.png` | Admin · System (Z3 active) |
| `04-playground-sqllab.png` / `05-playground-result.png` | SQL Diagnose Labor + result |
| `06-dataset-playground.png` / `07-dataset-generation-failed.png` | Dataset Playground + failure |
| `08-instructor-dashboard.png` | Instructor dashboard |
| `09-instructor-schemas.png` / `10-schema-metadata.png` | Schema upload + metadata |
| `11-instructor-assignment-created.png` | Assignment created (LIVE) |
| `12-student-dashboard.png` / `13-student-question-editor.png` | Student dashboard + editor |
| `14-student-after-grading.png` | Student after grading (10/10, Level 2) |
| `15-light-mode.png` | Light mode (dark editor bug) |
| `16-instructor-stats-broken.png` | Instructor statistics broken |
