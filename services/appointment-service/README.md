# Appointment Service

Appointment schedules visits, updates appointments, and applies status changes. It stores patient and doctor UUIDs in its own PostgreSQL database.

## API

Use the gateway at `http://localhost:8222` with `/api/v1/appointments`.
Send a bearer access token with the authority shown below.

| Method | Route | Access | Success | Purpose |
| --- | --- | --- | ---: | --- |
| `GET` | `/api/v1/appointments` | `APPOINTMENT_READ` | 200 | List appointments with pagination |
| `GET` | `/api/v1/appointments/{id}` | `APPOINTMENT_READ` | 200 | Get an appointment by UUID |
| `POST` | `/api/v1/appointments` | `APPOINTMENT_CREATE` | 201 | Create an appointment |
| `PUT` | `/api/v1/appointments/{id}` | `APPOINTMENT_UPDATE` | 200 | Update schedule, type, reason, and notes |
| `PATCH` | `/api/v1/appointments/status/{id}` | `APPOINTMENT_CANCEL` | 200 | Request a status transition |

## Behavior

The [appointment creation sequence](../../documents/architecture/create-appointment.html) shows the gateway request, patient and doctor lookups, overlap check, and database write. Download the HTML to open the interactive diagram locally.

- On creation, the service calls Patient and Doctor through OpenFeign to verify that both records exist and are active. It forwards the caller’s bearer token, so the caller also needs `PATIENT_READ` and `DOCTOR_READ` for these lookups.
- Creation and schedule updates check for overlapping appointments for the same doctor and date. The overlap query includes cancelled appointments and excludes the appointment being updated. The check runs before the write in application code.
- New appointments start in `SCHEDULED`. The status endpoint accepts requests for `CONFIRMED`, `COMPLETED`, `CANCELLED`, and `NO_SHOW`, subject to domain transition rules. All four actions currently use the endpoint authority `APPOINTMENT_CANCEL`.
- Appointment lists default to 20 results per page sorted by ID.

## Configuration

Use `APPOINTMENT_SERVER_DB_*`, `APPOINTMENT_SERVER_PORT`, and `APPOINTMENT_SERVER_PROFILES` with the shared JWT signing key. The creation flow needs both Patient and Doctor reachable through service discovery.

See the [platform setup](../../README.md#run-with-docker-compose) and [Docker environment template](../../deployment/env/.env.docker.example).

## Tests and coverage

The November 25, 2026 run at `f9c0300` passed **140 tests** with zero failures, errors, or skips. JaCoCo measured **90.6% line coverage** and **77.4% branch coverage**. The service met its configured coverage checks.

The saved HTML snapshot is local to this workspace.

- JaCoCo — `documents/appointment-service/jacoco/index.html`
- Surefire — `documents/appointment-service/surefire/surefire.html`

From the repository root, run the verification with Docker available for Testcontainers. The second command renders Surefire HTML from the test results.

```sh
mvn -pl services/appointment-service -am clean verify
mvn -pl services/appointment-service surefire-report:report-only
```

Fresh reports are generated under this module’s `target/site/jacoco/` and `target/reports/` directories.
