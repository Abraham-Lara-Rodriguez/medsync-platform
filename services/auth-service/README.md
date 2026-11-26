# Auth Service

Auth manages user accounts and issues JWT access and refresh tokens. It persists refresh sessions in PostgreSQL and uses BCrypt for user passwords.

## API

Use the gateway at `http://localhost:8222` with `/api/v1/auth` and `/api/v1/users`.

| Method | Route | Access | Success | Purpose |
| --- | --- | --- | ---: | --- |
| `POST` | `/api/v1/auth/login` | Public | 200 | Authenticate with email and password |
| `POST` | `/api/v1/auth/refresh-token` | Public | 200 | Exchange a valid refresh token for a new token pair |
| `POST` | `/api/v1/auth/logout` | Bearer access token | 204 | Revoke the supplied refresh token |
| `GET` | `/api/v1/users` | `USER_READ` | 200 | List users with pagination |
| `GET` | `/api/v1/users/search` | `USER_READ` | 200 | Search by text, role, or status |
| `GET` | `/api/v1/users/{id}` | `USER_READ` | 200 | Get a user by UUID |
| `POST` | `/api/v1/users` | `USER_CREATE` | 201 | Create a user account |
| `PUT` | `/api/v1/users/{id}` | `USER_UPDATE` | 200 | Update an account |

## Behavior

- Login returns `accessToken` and `refreshToken`. Send the access token as `Authorization: Bearer <access-token>` for protected endpoints.
- Refresh and logout accept a JSON body with `refreshToken`. Refresh revokes the previous token and stores its replacement. Logout revokes the supplied refresh token. Other business services validate an issued access token until its configured expiration.
- The role definitions are `ADMIN`, `RECEPTIONIST`, `DOCTOR`, and `PATIENT`. The endpoint table lists the authorities checked by the controllers.

## Configuration

Use `AUTH_SERVER_DB_*`, `AUTH_SERVER_PORT`, `AUTH_SERVER_PROFILES`, and `AUTH_SERVER_SECRET_KEY`. Token lifetimes are in milliseconds. Set `BOOTSTRAP_ADMIN_ENABLED=true` with an email and password to initialize an administrator in a fresh environment. Flyway migrations create the users and refresh token tables.

See the [platform setup](../../README.md#run-with-docker-compose) and [Docker environment template](../../deployment/env/.env.docker.example).

## Tests and coverage

The November 25, 2026 run at `f9c0300` passed **131 tests** with zero failures, errors, or skips. JaCoCo measured **94.9% line coverage** and **89.3% branch coverage**. The service met its configured coverage checks.

The saved HTML snapshot is local to this workspace.

- JaCoCo — `documents/auth-service/jacoco/index.html`
- Surefire — `documents/auth-service/surefire/surefire.html`

From the repository root, run the verification with Docker available for Testcontainers. The second command renders Surefire HTML from the test results.

```sh
mvn -pl services/auth-service -am clean verify
mvn -pl services/auth-service surefire-report:report-only
```

Fresh reports are generated under this module’s `target/site/jacoco/` and `target/reports/` directories.
