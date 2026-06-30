# XData-Web — Automated SQL Query Grading Platform

XData-Web is a web platform for **automatically grading SQL query assignments**.
It is a modernized version of the [XData](http://www.cse.iitb.ac.in/infolab/xdata)
system originally developed at the InfoLab, IIT Bombay: the scientifically
validated grading core is preserved, while the surrounding application has been
rebuilt as a layered **Spring Boot 3** backend and a separate **React** single-page
frontend.

The project was developed as a Bachelor's thesis at FH Vorarlberg
("Modernizing the XData SQL Query Validation System for Educational Use").

## Why it is more than string comparison

A SQL query can be syntactically valid, return the right answer on the
instructor's sample data, and still be *wrong* on a different instance of the
same schema. XData-Web grades by **semantic equivalence**, not by comparing query
text:

- **Equivalence chain** — each submission is checked through an ordered fallback
  of rungs, cheap-and-pure first: `text-equality → test-data-execution → SMT (Z3)
  → assigned-DB`. The first decisive rung wins.
- **Three-state verdict** — `EQUIVALENT`, `NOT_EQUIVALENT`, or `INCONCLUSIVE`.
  Crucially, a missing decision never silently collapses to "wrong"; it stays
  inconclusive instead of zeroing a potentially correct answer.
- **Partial marking** — even a non-equivalent query is scored structurally
  (projections, predicates, joins, group-by, …) so a near-miss still earns
  partial credit, independent of the equivalence verdict.

See [`CONTEXT.md`](CONTEXT.md) for the full domain and architecture glossary.

## Features

- **Automated grading** of SQL submissions with instant feedback.
- **Partial credit** via structural scoring, plus attempt/timing **penalties** and
  **XP** gamification for improvement over a student's previous best.
- **Courses, Assignments, Questions, Submissions** with instructor and student
  workflows.
- **Instructor playground** — dry-run a pattern/answer pair with custom weights and
  no persistence.
- **Live grading** over WebSocket (STOMP) and an in-browser **Monaco SQL editor**
  with schema-aware auto-completion.
- **LMS integration** — grade upload via LTI.
- **Role-based access** — `ADMIN`, `INSTRUCTOR`, `STUDENT`, enforced by JWT auth and
  a central course-access guard.
- **Dashboards & analytics** — leaderboards, assignment statistics, CSV export.

## Tech stack

| Layer | Technologies |
|-------|--------------|
| **Backend** | Spring Boot 3 (Java 17), Spring Security 6 + JWT (jjwt), Spring Data JPA, PostgreSQL, Flyway migrations, Z3 (`z3-turnkey`) for the SMT rung, embedded Apache Derby for ephemeral scratch databases, JSQLParser, WebSocket/STOMP, springdoc/Swagger |
| **Frontend** | React 18 + TypeScript, React Router 7, Monaco Editor, CodeMirror (SQL), Recharts, Framer Motion, STOMP/SockJS, axios |
| **Build & infra** | Maven, Create React App, Docker & Docker Compose, Testcontainers + JUnit |

## Architecture at a glance

The backend is layered with a **ports-and-adapters** boundary around impure work:

- **`SubmissionEvaluation`** — the deep module that orchestrates grading for one
  submission. Its **decision core** is a pure function
  (`evaluate(EvaluationRequest) → GradingOutcome`): no JPA, no I/O, unit-testable
  with zero infrastructure. Side effects (persistence, WebSocket notification, LMS
  publish) go through ports, each with a production adapter and an in-memory fake.
- **`EquivalenceStageCheck`** — one rung of the equivalence chain; never throws,
  an internal failure becomes `INCONCLUSIVE`.
- **`QueryRunner` / `ResultRows`** — the single home for running a query and
  comparing result sets (order-insensitive multiset equality).
- **`ScratchDatabase`** — ephemeral in-memory Derby database for test-data execution.
- **`CourseAccessGuard`** — the single seam for authorization in controllers.
- **`com.xdata.legacy.*`** — the preserved, validated XData core (mutation-based
  test-data generation, SMT constraint generation, partial marking).

Architecture decisions are recorded in [`docs/adr/`](docs/adr).

## Quick start

**The only thing you need installed is [Docker](https://www.docker.com/get-started)**
(Docker Desktop already includes Docker Compose). You do **not** need to install
Java, Node, PostgreSQL, or the Z3 solver — everything runs in containers.

From the repository root:

```bash
docker compose up --build        # add -d to run in the background
```

That single command builds and starts all three services — PostgreSQL, the
Spring Boot backend, and the React frontend. On first run the backend creates the
database schema and a default admin user automatically (Flyway migrations), so
there is **no manual database setup**. The first build takes a few minutes; later
starts are fast.

When the logs settle, open **<http://localhost>** and log in (see
[credentials](#default-credentials) below).

```bash
docker compose down        # stop everything
docker compose down -v     # stop AND wipe the database (fresh start next time)
```

> **Ports used:** `80` (frontend), `8080` (backend API), `5433` (database).
> Make sure these are free. A local PostgreSQL on `5432` will **not** clash — the
> database container is published on `5433`.

### Where things are

| Service | URL |
|---------|-----|
| Frontend (the app) | <http://localhost> |
| Backend API | <http://localhost:8080/api/v1> |
| Swagger UI (API docs) | <http://localhost:8080/swagger-ui.html> |
| Database | `localhost:5433` (Postgres, user `postgres` / password `1709`) |

### Default credentials

A single administrator account is seeded automatically:

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin1` | `admin1` |

Log in as `admin1` and create instructor and student accounts from the admin
panel (User Management). There are no other pre-seeded users.

## Local development (optional)

Only needed if you want to run a service **outside** Docker for faster iteration.
For just trying the app, the [Quick start](#quick-start) above is enough.

You still don't have to install PostgreSQL — start only the database container and
point the locally-run service at it:

```bash
docker compose up -d db        # Postgres on localhost:5433
```

### Backend

Requires **JDK 21** (targets Java 17 bytecode). It reads its config from
`application.properties`, which defaults to `localhost:5432`, so either run the DB
on 5432 or override the host/port via env vars:

```bash
cd xdata-web/backend
XDATA_DB_PORT=5433 mvn spring-boot:run     # use the Docker db from `compose up -d db`
mvn test                                   # run the test suite
```

### Frontend

Requires Node.js (the project builds on Node 18+). The dev server talks to the
backend on port `8080`:

```bash
cd xdata-web/frontend
npm install
npm start               # dev server on http://localhost:3000
npm run build           # production bundle
```

## Configuration

The system is configured via environment variables (see `docker-compose.yml`):

| Variable | Description | Default |
|----------|-------------|---------|
| `XDATA_DB_HOST` | Database host | `db` (Docker) / `localhost` (local) |
| `XDATA_DB_PORT` | Database port | `5432` |
| `XDATA_DB_NAME` | Database name | `xdatadb` |
| `XDATA_DB_USER` | Database user | `postgres` |
| `XDATA_DB_PASS` | Database password | `1709` |
| `HIBERNATE_DDL_AUTO` | Hibernate DDL mode (Flyway owns the schema) | `validate` |
| `JWT_SECRET` | Secret key for signing JWTs | built-in dev default (set your own in production) |
| `ALLOWED_ORIGINS` | CORS allowed origins | `http://localhost:3000,http://localhost:80,http://localhost` |

All variables have working defaults, so `docker compose up --build` runs with zero
configuration. Override them only for a real deployment.

## Troubleshooting

| Symptom | Cause & fix |
|---------|-------------|
| `Bind for 0.0.0.0:80 failed: port is already allocated` (or `:8080` / `:5433`) | Another process holds the port. Stop it, or remap the host port in `docker-compose.yml` (e.g. `"8081:80"` for the frontend) and use the new port. |
| Frontend loads but every request fails / "Network Error" | The backend isn't up yet or port `8080` is blocked. Wait for the `backend` healthcheck to pass, then reload. Check `docker compose logs backend`. |
| Backend container exits or restarts on boot | Usually the database wasn't ready, or a schema mismatch. Check `docker compose logs backend`; for a clean slate run `docker compose down -v && docker compose up --build`. |
| Login as `admin1` fails | The DB was created by an older run. Reset it: `docker compose down -v` then `docker compose up`. |
| Build is killed / runs out of memory | Give Docker more RAM (Docker Desktop → Settings → Resources → Memory, ~4 GB+). |
| First build is very slow | Normal — it compiles the backend and frontend from scratch. Subsequent starts reuse the cached images. |
| Changed code but don't see it | Rebuild the images: `docker compose up --build` (plain `up` reuses old images). |
| Inspect the database directly | Connect any Postgres client to `localhost:5433`, db `xdatadb`, user `postgres`, password `1709`. |

## Project structure

```
xdata-web/
├── xdata-web/backend       Spring Boot application (grading core, REST API, security)
├── xdata-web/frontend      React + TypeScript single-page app
├── docs/adr/               Architecture Decision Records
├── uploads/                Shared volume for schema and data uploads
├── docker-compose.yml      Database + backend + frontend orchestration
└── CONTEXT.md              Domain & architecture glossary
```

## License, attribution and disclaimer

### Provenance

This project is a **derivative work** of the **XData / XData-Grading** system
developed by the InfoLab at the **Indian Institute of Technology Bombay
(IIT Bombay)**:

- Upstream source: <https://gitlab.com/xdata/xdata-web>
- Project site: <http://www.cse.iitb.ac.in/infolab/xdata/>

The upstream XData is licensed under the **Apache License 2.0**, which expressly
permits creating, using and distributing modified and derivative works. This
repository exercises that grant.

### Licensing of this repository

- The whole repository is distributed under the **Apache License 2.0** — see
  [`LICENSE`](LICENSE) and [`NOTICE`](NOTICE).
- The reused XData core (under `xdata-web/backend/.../com/xdata/legacy/` and the
  bundled `xdata-core` artifact) remains **© 2019 xdata (IIT Bombay InfoLab)**,
  Apache-2.0. It has been **modified** as part of this modernization.
- All new code and the modifications are **© 2026 Daniel Shahini**, Apache-2.0.

If you redistribute this software, you must retain the `LICENSE` and `NOTICE`
files and the attributions above, as required by the Apache License 2.0.

### Third-party components

This project bundles or depends on the following, each under its own permissive
license: **Z3** (MIT), **Apache Derby** and **JSQLParser** (Apache-2.0),
**dk.brics.automaton** (BSD), **Spring Boot / Spring Security** (Apache-2.0),
**React** (MIT), and other libraries declared in `pom.xml` and
`package.json`. All trademarks are the property of their respective owners.

### No affiliation

This is an independent project. It is **not affiliated with, endorsed by, or
sponsored by IIT Bombay** or the XData authors. The name "XData" is used only to
identify the upstream system from which this work derives.

### Disclaimer of warranty

This software is provided **"AS IS", without warranty of any kind**, express or
implied, as set out in Sections 7–8 of the Apache License 2.0. It was created in
an academic context (a Bachelor's thesis) and is **not hardened for production**.
You use it at your own risk; the authors accept no liability for any damage or
data loss arising from its use.

### Security note for public deployments

The default configuration ships **development-only credentials** — the seeded
`admin1` account, the database password, and the built-in JWT signing key (see
[Configuration](#configuration)). These exist only to make local startup
frictionless. **Before exposing any instance to a network, you must** change the
admin password, set a strong `JWT_SECRET`, and use real database credentials.
Do not run the default configuration on a public host.

*This section is provided for transparency and is not legal advice.*
