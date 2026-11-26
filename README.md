# MedSync Platform

[![CI](https://github.com/Abraham-Lara-Rodriguez/medsync-platform/actions/workflows/ci.yml/badge.svg)](https://github.com/Abraham-Lara-Rodriguez/medsync-platform/actions/workflows/ci.yml)
![GitHub repo size](https://img.shields.io/github/repo-size/Abraham-Lara-Rodriguez/medsync-platform)
![GitHub stars](https://img.shields.io/github/stars/Abraham-Lara-Rodriguez/medsync-platform?style=social)
![GitHub last commit](https://img.shields.io/github/last-commit/Abraham-Lara-Rodriguez/medsync-platform)
![GitHub license](https://img.shields.io/github/license/Abraham-Lara-Rodriguez/medsync-platform)

MedSync is a healthcare scheduling backend built with Java 17, Spring Boot, Spring Cloud, and PostgreSQL. It manages user accounts, patient records, doctor records, and appointments through a single API gateway.

## Architecture

[![MedSync deployment diagram showing the gateway, business services, and PostgreSQL databases](documents/architecture/deployment.svg)](documents/architecture/deployment.svg)

Open the [interactive deployment diagram](documents/architecture/deployment.html) or the [appointment creation sequence](documents/architecture/create-appointment.html) in a browser. On GitHub, download the HTML file and open it locally to use the interactive viewer. The editable Archify definitions are [deployment.json](documents/architecture/deployment.json) and [create-appointment.json](documents/architecture/create-appointment.json).

| Component | Responsibility |
| --- | --- |
| [API Gateway](gateway/api-gateway/README.md) | Publishes the HTTP entry point and routes requests to services discovered through Eureka. |
| [Config Server](infrastructure/config-service/README.md) | Serves shared and profile-specific Spring configuration. |
| [Discovery Service](infrastructure/discovery-service/README.md) | Runs the Eureka service registry. |
| [Auth Service](services/auth-service/README.md) | Manages user accounts, roles, JWT access tokens, and refresh sessions. |
| [Patient Service](services/patient-service/README.md) | Registers, updates, and deactivates patient records. |
| [Doctor Service](services/doctor-service/README.md) | Maintains doctor profiles, specialties, and active status. |
| [Appointment Service](services/appointment-service/README.md) | Schedules and updates appointments, validates patient and doctor records, and applies status transitions. |
| [Shared libraries](shared/) | Provide JWT validation and common API error handling. |

The gateway routes the five API families under `/api/v1` to the four business services. Each business service owns its PostgreSQL database and Flyway migrations. Docker Compose runs PostgreSQL 18 and Grafana Alloy. Instrumented services send OTLP to Alloy, which forwards traces to the configured destination.

## Run with Docker Compose

Run these PowerShell commands from the repository root with Docker running.

```powershell
if (-not (Test-Path deployment/env/.env.docker)) {
    Copy-Item deployment/env/.env.docker.example deployment/env/.env.docker
}
```

If you already have a local environment file, keep it and use the [Docker template](deployment/env/.env.docker.example) as a reference. Set matching Config Server and Eureka credentials, database passwords, the JWT signing key, and the patient and doctor encryption and hash keys. Enable administrator bootstrap and set its credentials if you need an initial account in a fresh database.

```powershell
docker compose --env-file deployment/env/.env.docker -f deployment/docker-compose.yml config --quiet
docker compose --env-file deployment/env/.env.docker -f deployment/docker-compose.yml up -d --build
curl.exe -i http://localhost:8222/actuator/health
```

Use `http://localhost:8222/api/v1` for business requests. The [local environment template](deployment/env/.env.example) provides values for running the JVM services outside Docker. The four business services enforce endpoint authorities using bearer access tokens. The current patient and appointment APIs authorize at the endpoint level. Deployments that expose patient accounts need a record ownership policy.

## Verify the system

Run the Maven reactor from the repository root with Java 17, Maven, and Docker available. Docker is used by integration suites that provision temporary PostgreSQL containers.

```sh
mvn clean verify
```

On November 25, 2026, the four business-service suites at `f9c0300` passed **495 tests**, and each met its configured JaCoCo checks. The service README files list the results and explain how to generate the HTML reports under each module's `target/` directory.

With the stack running, use the [flow runner](deployment/script/README.md) to exercise the deployed APIs through the gateway and export a JSON execution record. The full scenario creates synthetic records and leaves them in the database for inspection.
