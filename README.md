# XData-Web: Automated SQL Query Grading System

This project is a modernized web-based version of the XData system, originally developed at IIT Bombay. It provides an automated way to grade SQL query assignments, support partial marking, and integrate with LMS via LTI.

## Features
- **Automated Grading:** Instant feedback and grading for SQL assignments.
- **Partial Marking:** Intelligent algorithms to award partial credit based on query logic.
- **Dockerized:** Easy to set up and deploy using Docker and Docker Compose.
- **Scalable:** Built with Spring Boot (Backend) and React (Frontend).

## Prerequisites
- [Docker](https://www.docker.com/get-started)
- [Docker Compose](https://docs.docker.com/compose/install/)

## Getting Started

To start the entire system (Database, Backend, and Frontend), run the following command from the root directory:

```bash
docker-compose up --build
```

*Note: Use `docker-compose up -d --build` if you want to run the services in the background.*

### Accessing the applications:
- **Frontend:** [http://localhost](http://localhost) (Port 80)
- **Backend API:** [http://localhost:8080/api/v1](http://localhost:8080/api/v1)
- **Swagger UI (API Docs):** [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
- **Database:** `localhost:5433` (External port redirected to 5432 internally)

### Troubleshooting: Port Conflicts
If you have a local PostgreSQL instance running, it might occupy port `5432`. To avoid this, the `docker-compose.yml` is configured to map the internal Postgres port to **5433** on your host machine.

### Default Credentials:
After starting the system, you can log in with the following test accounts:
- **Admin:** `admin1` / `admin1`
- **Instructor:** `daniel` / `daniel`

## Configuration

The system can be customized via environment variables in the `docker-compose.yml` file.

| Variable | Description | Default |
|----------|-------------|---------|
| `SPRING_DATASOURCE_URL` | JDBC URL for PostgreSQL | `jdbc:postgresql://db:5432/xdatadb` |
| `SPRING_DATASOURCE_USERNAME` | Database User | `postgres` |
| `SPRING_DATASOURCE_PASSWORD` | Database Password | `1709` |
| `JWT_SECRET` | Secret key for JWT | (Random default provided) |
| `ALLOWED_ORIGINS` | CORS allowed origins | `http://localhost:3000, http://localhost:80` |

## Project Structure
- `xdata-web/backend`: Spring Boot application containing the grading logic.
- `xdata-web/frontend`: React application for students and instructors.
- `Docs/`: Technical documentation and sample data.
- `uploads/`: Shared volume for schema and data uploads.

## Development

If you want to run the components separately for development:

### Backend:
Requires Java 17 and PostgreSQL.
```bash
cd xdata-web/backend
mvn spring-boot:run
```

### Frontend:
Requires Node.js.
```bash
cd xdata-web/frontend
npm install
npm start
```

## Credits & Documentation
The original XData system was developed by the InfoLab at IIT Bombay.
- Website: [http://www.cse.iitb.ac.in/infolab/xdata](http://www.cse.iitb.ac.in/infolab/xdata)
- Contact: xdata@cse.iitb.ac.in

For detailed technical documentation, please refer to the `Docs/` directory or visit the official website.