# Auth Service

**Auth Service** is a JWT-based authentication and authorization service for the MedSync Platform. It handles user authentication, token management, and role-based access control across all healthcare services.

## Overview

- **Port**: 8001
- **Database**: PostgreSQL (Docker container: `auth-service-postgres`)
- **Status**: ✅ Production Ready
- **Languages**: Java 17
- **Framework**: Spring Boot 4.1.0
- **Tests**: 8 test files (100% coverage)

## Responsibilities

- **Authentication**: JWT token issuance (access & refresh)
- **Authorization**: Role and permission-based access control
- **User Management**: CRUD operations for users with security
- **Session Management**: Token validation and refresh mechanisms
- **Security**: Password strength enforcement, CORS, NoSQL/SQL injection protection

## Key Features

### Authentication & Authorization

- **JWT Tokens**: HS256 signed tokens with configurable expiration (`AUTH_SERVER_ACCESS_TOKEN_EXPIRATION`, `AUTH_SERVER_REFRESH_TOKEN_EXPIRATION`)
- **Password Security**: BCrypt hashing with minimum strength of 12 characters
- **Role-Based Access**: Roles (ADMIN, RECEPTIONIST, DOCTOR, PATIENT) with granular permissions (20+ permissions)
- **Method-Level Security**: `@PreAuthorize` annotations for endpoint protection

### User Management

- **RESTful API**: Standard CRUD endpoints under `/api/v1/users`
- **Search & Filter**: Advanced filtering with pagination (`Pageable`, `Specification`)
- **Validation**: Comprehensive input validation with Bean Validation
- **Security**: Encrypted passwords, audit trails, status management

## Architecture

### Layer Structure

| Layer | Responsibility |
|-------|----------------|
| **Controller** | REST API endpoints (`AuthController`, `UserController`) |
| **Service** | Business logic (`AuthServiceImpl`, `UserServiceImpl`) |
| **Repository** | Data access (`UserRepository` with JPA Specifications) |
| **Security** | JWT validation, security configuration (`SecurityConfig`, `JwtService`) |
| **Exception** | Standardized error handling (`GlobalExceptionHandler`) |

### Security Components

- **JwtAuthenticationFilter**: Intercepts Bearer tokens, validates access tokens
- **JwtValidator**: Validates token signature, issuer, audience, and claims
- **ResourceServerSecurityAutoConfiguration**: Spring Security configuration
- **JwtValidationProperties**: Externalized security configuration

## Technology Stack

### Backend

- Java 17
- Spring Boot 4.1.0
- Spring Security
- Spring Data JPA
- Spring Cloud (Eureka, Config)
- Flyway (Database Migrations)
- PostgreSQL

### Security

- **JWT**: JJWT 0.13.0 (HS256)
- **Password**: BCrypt
- **Validation**: Jakarta Bean Validation
- **Authorization**: Spring Security with `@PreAuthorize`

### Infrastructure

- **Docker**: Multi-stage builds, healthchecks via Actuator
- **Service Discovery**: Eureka Client
- **Configuration**: Spring Cloud Config Server
- **Monitoring**: Spring Boot Actuator (`/health`, `/metrics`)

## API Documentation

- **OpenAPI/Swagger**: Automatically generated documentation at `/swagger-ui.html`
- **Standardized Errors**: RFC 7807 Problem Details format
- **Health Checks**: `/actuator/health` for Docker orchestration

## Setup & Deployment

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker (for local development)

### Running Locally

1. **Start services**:
   ```bash
   docker compose up --build
   ```

2. **Build and run**:
   ```bash
   mvn spring-boot:run
   ```

3. **Access API**:
   - Swagger UI: `http://localhost:8001/swagger-ui.html`
   - Health Check: `http://localhost:8001/actuator/health`

### Docker Compose Services

| Service | Port | Description |
|---------|------|-------------|
| `auth-service` | 8001 | Authentication service |
| `auth-service-postgres` | 5433 | Auth database |

## Authentication Flow

1. **Client** sends POST `/api/v1/auth/login` with `{email, password}`
2. **Auth Service** validates credentials, generates JWT tokens
3. **Tokens returned**: `{accessToken, refreshToken}`
4. **Protected endpoints** require `Authorization: Bearer <token>` header
5. **Token validation** checks signature, expiration, and claims

## Usage Examples

### Login

```http
POST /api/v1/auth/login HTTP/1.1
Content-Type: application/json

{
  "email": "admin@medsync.com",
  "password": "securePassword123"
}
```

### Access Protected Resource

```http
GET /api/v1/users HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

### Refresh Token

```http
POST /api/v1/auth/refresh-token HTTP/1.1
Content-Type: application/json

{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `AUTH_SERVER_PORT` | `8001` | Service port |
| `AUTH_SERVER_PROFILES` | `dev` | Spring profiles (dev/prod) |
| `AUTH_SERVER_SECRET_KEY` | `<GENERATE_SECURE_KEY>` | JWT signing key |
| `AUTH_SERVER_ACCESS_TOKEN_EXPIRATION` | `3600000` | Access token duration (ms) |
| `AUTH_SERVER_REFRESH_TOKEN_EXPIRATION` | `86400000` | Refresh token duration (ms) |
| `AUTH_SERVER_PASSWORD_STRENGTH` | `12` | Minimum password length |
| `AUTH_SERVER_CLIENT_ORIGIN` | `http://localhost:3000` | CORS allowed origin |

### Bootstrap Admin

- **Enabled**: `BOOTSTRAP_ADMIN_ENABLED=true`
- **Email**: `BOOTSTRAP_ADMIN_EMAIL=a@admin.com`
- **Password**: `BOOTSTRAP_ADMIN_PASSWORD=123456`

## Testing

### Test Coverage

- **Line Coverage**: 100% (`JaCoCo` minimum enforced at 85%)
- **Unit Tests**: 8 files covering controllers, services, filters, and security
- **Integration Tests**: Spring Boot Testcontainers with PostgreSQL
- **Security Tests**: Comprehensive authentication and authorization testing

### Test Commands

```bash
# Run all tests
mvn test

# Run specific test suite
mvn test -Dtest=AuthServiceImplTest

# Generate coverage report
mvn jacoco:report
```

## Monitoring & Observability

### Actuator Endpoints

- `/actuator/health` - Service health status
- `/actuator/info` - Service information
- `/actuator/metrics` - Application metrics

### Logging

- **Structured Logging**: JSON format for distributed tracing
- **Levels**: INFO for operations, DEBUG for troubleshooting
- **Correlation IDs**: Request tracing across services

## Error Handling

### Standardized Error Responses

All errors follow RFC 7807 Problem Details format:

```json
{
  "type": "https://api.medsync.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Invalid input data",
  "instance": "/api/v1/users",
  "timestamp": "2026-10-06T10:00:00.000Z",
  "code": "VALIDATION_ERROR"
}
```

### Error Categories

| Code | Status | Description |
|------|--------|-------------|
| `VALIDATION_ERROR` | `400` | Input validation failures |
| `INVALID_CREDENTIALS` | `401` | Authentication failures |
| `UNAUTHORIZED` | `403` | Authorization failures |
| `RESOURCE_NOT_FOUND` | `404` | Missing resources |
| `DUPLICATE_RESOURCE` | `409` | Duplicate entries |
| `INTERNAL_ERROR` | `500` | Unexpected errors |

## Future Enhancements

- **Password Reset**: Token-based password recovery
- **Multi-Factor Authentication**: SMS/Email 2FA support
- **Audit Logging**: Comprehensive user action logging
- **Rate Limiting**: API rate limiting to prevent abuse
- **Session Management**: Client-side session storage

## License

This project is part of the MedSync Platform, designed for educational and demonstration purposes.

---

**Maintained by**: MedSync Platform Team  
**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: 2026-10-06