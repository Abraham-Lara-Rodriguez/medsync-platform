# Patient Service

Patient manages demographic and contact records used by scheduling. The service stores patient records in a dedicated PostgreSQL database.

## API

Use the gateway at `http://localhost:8222` with `/api/v1/patients`.
Send a bearer access token with the authority shown below.

| Method | Route | Access | Success | Purpose |
| --- | --- | --- | ---: | --- |
| `GET` | `/api/v1/patients` | `PATIENT_READ` | 200 | List patients with pagination |
| `GET` | `/api/v1/patients/{id}` | `PATIENT_READ` | 200 | Get a patient by UUID |
| `POST` | `/api/v1/patients` | `PATIENT_CREATE` | 201 | Register a patient |
| `PUT` | `/api/v1/patients/{id}` | `PATIENT_UPDATE` | 200 | Update a patient record |
| `PATCH` | `/api/v1/patients/deactivate/{id}` | `PATIENT_DEACTIVATE` | 204 | Deactivate a patient record |

## Behavior

- Patient lists default to 20 results per page sorted by first name. The service caps page size at 100.
- Document number, phone, and email are encrypted at rest. Separate deterministic hashes support uniqueness checks.
- Deactivation changes the patient record status. Appointment checks that the patient is active before creating a booking.

## Configuration

Use `PATIENT_SERVER_DB_*`, `PATIENT_SERVER_PORT`, and `PATIENT_SERVER_PROFILES` with the shared JWT signing key. Compose maps `PATIENT_ENCRYPTION_KEY` and `PATIENT_HASH_KEY` to `ENCRYPTION_KEY` and `MEDSYNC_HASH_KEY`. Keep those keys with the data they protect.

See the [platform setup](../../README.md#run-with-docker-compose) and [Docker environment template](../../deployment/env/.env.docker.example).

## Tests and coverage

The November 25, 2026 run at `f9c0300` passed **120 tests** with zero failures, errors, or skips. JaCoCo measured **98.3% line coverage** and **98.5% branch coverage**. The service met its configured coverage checks.

The saved HTML snapshot is local to this workspace.

- JaCoCo — `documents/patient-service/jacoco/index.html`
- Surefire — `documents/patient-service/surefire/surefire.html`

From the repository root, run the verification with Docker available for Testcontainers. The second command renders Surefire HTML from the test results.

```sh
mvn -pl services/patient-service -am clean verify
mvn -pl services/patient-service surefire-report:report-only
```

Fresh reports are generated under this module’s `target/site/jacoco/` and `target/reports/` directories.
