# Doctor Service

Doctor manages doctor profiles, specialties, license details, and active status in its own PostgreSQL database.

## API

Use the gateway at `http://localhost:8222` with `/api/v1/doctors`.
Send a bearer access token with the authority shown below.

| Method | Route | Access | Success | Purpose |
| --- | --- | --- | ---: | --- |
| `GET` | `/api/v1/doctors` | `DOCTOR_READ` | 200 | List doctors with pagination |
| `GET` | `/api/v1/doctors/{id}` | `DOCTOR_READ` | 200 | Get a doctor by UUID |
| `POST` | `/api/v1/doctors` | `DOCTOR_CREATE` | 201 | Register a doctor |
| `PUT` | `/api/v1/doctors/{id}` | `DOCTOR_UPDATE` | 200 | Update name, contact details, and status |

## Behavior

- Doctor lists default to 20 results per page sorted by first name.
- Medical license, email, and phone are encrypted at rest. Separate deterministic hashes support uniqueness checks.
- The create request accepts specialty and medical license. The update request accepts name, email, phone, and status. Appointment checks that the doctor is active before creating a booking.

## Configuration

Use `DOCTOR_SERVER_DB_*`, `DOCTOR_SERVER_PORT`, and `DOCTOR_SERVER_PROFILES` with the shared JWT signing key. Compose maps `DOCTOR_ENCRYPTION_KEY` and `DOCTOR_HASH_KEY` to `ENCRYPTION_KEY` and `MEDSYNC_HASH_KEY`. Keep those keys with the data they protect.

See the [platform setup](../../README.md#run-with-docker-compose) and [Docker environment template](../../deployment/env/.env.docker.example).

## Tests and coverage

The November 25, 2026 run at `f9c0300` passed **104 tests** with zero failures, errors, or skips. JaCoCo measured **91.1% line coverage** and **83.3% branch coverage**. The service met its configured coverage checks.

The saved HTML snapshot is local to this workspace.

- JaCoCo — `documents/doctor-service/jacoco/index.html`
- Surefire — `documents/doctor-service/surefire/surefire.html`

From the repository root, run the verification with Docker available for Testcontainers. The second command renders Surefire HTML from the test results.

```sh
mvn -pl services/doctor-service -am clean verify
mvn -pl services/doctor-service surefire-report:report-only
```

Fresh reports are generated under this module’s `target/site/jacoco/` and `target/reports/` directories.
