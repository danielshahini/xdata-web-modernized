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

## Getting started (Docker)

Prerequisites: [Docker](https://www.docker.com/get-started) and
[Docker Compose](https://docs.docker.com/compose/install/).

From the repository root:

```bash
docker compose up --build        # add -d to run detached
```

This starts three services — PostgreSQL, the Spring Boot backend, and the React
frontend.

| Service | URL |
|---------|-----|
| Frontend | <http://localhost> (port 80) |
| Backend API | <http://localhost:8080/api/v1> |
| Swagger UI | <http://localhost:8080/swagger-ui.html> |
| Database | `localhost:5433` (mapped to container port 5432) |

> **Port conflict:** a local PostgreSQL instance on `5432` will not clash — the
> host port is mapped to **5433**.

### Default credentials

| Role | Username | Password |
|------|----------|----------|
| Admin | `admin1` | `admin1` |
| Instructor | `daniel` | `daniel` |

## Local development

### Backend

Requires a JDK (the build is run with **JDK 21**, targeting Java 17 bytecode) and a
reachable PostgreSQL instance.

```bash
cd xdata-web/backend
mvn spring-boot:run
mvn test                # run the test suite (Testcontainers spins up PostgreSQL)
```

### Frontend

Requires Node.js.

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
| `HIBERNATE_DDL_AUTO` | Hibernate DDL mode | `update` (Docker) / `validate` (local) |
| `JWT_SECRET` | Secret key for signing JWTs | random default |
| `ALLOWED_ORIGINS` | CORS allowed origins | `http://localhost:3000, http://localhost:80` |

## Project structure

```
xdata-web/
├── xdata-web/backend       Spring Boot application (grading core, REST API, security)
├── xdata-web/frontend      React + TypeScript single-page app
├── docs/                   Technical documentation, ADRs, audits
├── uploads/                Shared volume for schema and data uploads
├── docker-compose.yml      Database + backend + frontend orchestration
├── CONTEXT.md              Domain & architecture glossary
└── REQUIREMENTS.md         Requirements and UX audit
```

## Credits

The original XData system was developed by the InfoLab at IIT Bombay.

- Website: <http://www.cse.iitb.ac.in/infolab/xdata>
- Contact: xdata@cse.iitb.ac.in

This modernized platform was built by Daniel Shahini. Licensed under the terms in
[`LICENSE`](LICENSE).
