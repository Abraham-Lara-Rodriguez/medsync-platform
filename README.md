# MedSync Platform

A production-oriented healthcare backend platform built with **Spring Boot microservices**, **Spring Cloud**, **JWT authentication**, **PostgreSQL**, and **Docker**.

MedSync Platform is designed as a modular and scalable microservices architecture that demonstrates enterprise backend development practices, including centralized configuration, service discovery, API gateway routing, shared security, standardized error handling, database migrations, and containerized deployment.

## Architecture

The platform is organized as a Maven multi-module monorepo where infrastructure services, domain services, and shared libraries are separated into independent modules.

```text
                        +-----------------------+
                        |      API Gateway      |
                        |        :8222          |
                        +----------+------------+
                                   |
                 +-----------------+-----------------+
                 |                                   |
                 |                                   |
        +--------v--------+                 +--------v--------+
        |  Auth Service    |                 | Patient Service |
        |      :8001       |                 |      :8002      |
        +--------+--------+                 +--------+--------+
                 |                                   |
         PostgreSQL :5433                    PostgreSQL :5432

                 +-----------------------------------+
                 |        Discovery Service          |
                 |             :8761                |
                 +----------------+------------------+
                                  |
                 +----------------v------------------+
                 |          Config Service           |
                 |              :8888               |
                 +-----------------------------------+

             Shared libraries:
             - common-security
             - common-core
```

## Modules

| Module              | Description                                                                               |
| ------------------- | ----------------------------------------------------------------------------------------- |
| `auth-service`      | Authentication, JWT token issuance, refresh tokens, user management, and authorization    |
| `patient-service`   | Patient management, encrypted sensitive data storage, validation, and business rules      |
| `api-gateway`       | Central entry point, routing, load balancing, and service exposure                        |
| `discovery-service` | Eureka service registry for dynamic service discovery                                     |
| `config-service`    | Spring Cloud Config Server with centralized configuration                                 |
| `common-security`   | Shared JWT resource server validation and security configuration                          |
| `common-core`       | Shared Problem Details model, error codes, base exceptions, and global exception handling |
| `deployment`        | Docker Compose environment and deployment configuration                                   |

## Technology Stack

### Backend

* Java 17
* Spring Boot 4.1
* Spring Security
* Spring Data JPA
* Spring Cloud Gateway
* Spring Cloud Config
* Spring Cloud Netflix Eureka
* Flyway
* OpenAPI / Swagger

### Infrastructure

* PostgreSQL
* Docker
* Docker Compose
* Maven

### Testing

* JUnit 5
* Mockito

## Current Features

### Authentication & Authorization

* JWT access tokens
* Refresh token support
* Role and authority-based authorization
* Method-level security (`@PreAuthorize`)
* Secure password handling
* User management endpoints
* OpenAPI documentation

### Patient Management

* CRUD operations
* Pagination support
* Request validation
* Patient status management
* Sensitive document encryption
* Uniqueness validation
* Standardized API error responses

### Platform Infrastructure

* Centralized configuration
* Service discovery
* API gateway routing
* Shared security library
* Shared exception handling
* Database migrations
* Containerized deployment

## Getting Started

### Prerequisites

* Java 17+
* Maven 3.9+
* Docker
* Docker Compose

### Run with Docker

From the `deployment` directory:

```bash
docker compose up --build
```

The platform starts the following services:

| Service              | Port |
| -------------------- | ---- |
| Config Service       | 8888 |
| Discovery Service    | 8761 |
| API Gateway          | 8222 |
| Auth Service         | 8001 |
| Patient Service      | 8002 |
| PostgreSQL (Patient) | 5432 |
| PostgreSQL (Auth)    | 5433 |

## Project Structure

```text
medsync-platform/
├── api-gateway/
├── auth-service/
├── patient-service/
├── config-service/
├── discovery-service/
├── common-security/
├── common-core/
├── deployment/
├── pom.xml
├── .gitignore
└── .dockerignore
```

## Shared Libraries

### common-security

Provides reusable JWT validation and resource server configuration that can be consumed by any microservice requiring authentication.

### common-core

Provides a consistent error model across all services using RFC 7807 Problem Details, shared error codes, and centralized exception handling.

## Security Model

Authentication is centralized in the **Auth Service**, while resource services validate JWT tokens through the **common-security** module.

The authorization model is based on **roles and granular authorities**, allowing endpoint-level permission control through Spring Security.

## Design Principles

This project follows several enterprise-oriented architectural principles:

* Microservices architecture
* Separation of concerns
* Reusable shared modules
* Centralized configuration
* Standardized error responses
* Stateless authentication
* Container-first deployment
* Infrastructure decoupling

## Roadmap

Planned future enhancements include:

* Appointment Service
* Notification Service
* Audit Service
* Distributed tracing
* Prometheus & Grafana monitoring
* CI/CD pipelines
* Kubernetes deployment
* Event-driven communication
* Resilience patterns (Circuit Breaker / Retry)

## License

This project is intended for educational, portfolio, and architectural demonstration purposes.
