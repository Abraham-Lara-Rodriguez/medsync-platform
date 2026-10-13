# Doctor Service

**Doctor Service** manages doctor records with secure data handling, validation, and integration with the MedSync Platform ecosystem.

## Overview

- **Port**: 8003
- **Database**: PostgreSQL (Docker container: `doctor-service-postgres`)
- **Status**: ✅ Production Ready (recently updated)
- **Languages**: Java 17
- **Framework**: Spring Boot 4.1.0
- **Tests**: 3 test files (recently expanded with integration tests)

## Responsibilities

- **Doctor Management**: Full CRUD operations for doctor records
- **Data Encryption**: AES encryption for sensitive PII (contact info)
- **Data Integrity**: SHA-256 hashing for identifiers
- **Validation**: Comprehensive input validation and uniqueness checks
- **Inter-service Communication**: OpenFeign clients for appointment service integration

## Key Features

### Doctor Records

- **Fields**: First name, last name, specialization, credentials, contact info
- **Status Management**: Active/Inactive doctor states
- **Uniqueness**: Medical license number uniqueness enforced

### Data Security

- **AES Encryption**: Contact information encrypted at rest
- **Deterministic Hashing**: Identifiers hashed for privacy
- **Field-Level Protection**: Sensitive data never exposed in API responses

### Inter-service Communication

- **OpenFeign**: Declarative client for appointment service
- **Resilience4j**: Circuit breaker for fault tolerance
- **Fallbacks**: Graceful degradation when services are unavailable

### Validation

- **Input Validation**: Jakarta Bean Validation on all DTOs
- **Business Rules**: Specialization format, credential validation
- **Uniqueness**: Real-time duplicate detection

## Architecture

### Layer Structure

| Layer | Responsibility |
|-------|----------------|
| **Controller** | REST API endpoints (`DoctorController`) |
| **Service** | Business logic (`DoctorServiceImpl`, `DoctorUniquenessValidator`, `DoctorHashService`) |
| **Repository** | Data access (`DoctorRepository`) |
| **Domain** | Entities, enums, constants, converters |
| **Mapper** | MapStruct mapping between entities and DTOs |

### Security Components

- **EncryptedStringConverter**: JPA AttributeConverter for AES encryption/decryption
- **DeterministicHasher**: SHA-256 hashing for identifiers
- **DoctorHashService**: Hash computation and validation

## Technology Stack

### Backend

- Java 17
- Spring Boot 4.1.0
- Spring Data JPA
- Spring Cloud (Eureka, Config)
- Flyway (Database Migrations)
- PostgreSQL
- MapStruct 1.5.5.Final
- Jasypt 1.9.3

### Security

- **Encryption**: AES-256 via JPA converters
- **Hashing**: SHA-256
- **Validation**: Jakarta Bean Validation

### Infrastructure

- **Docker**: Multi-stage builds, healthchecks via Actuator
- **Service Discovery**: Eureka Client
- **Configuration**: Spring Cloud Config Server
- **Circuit Breaker**: Resilience4j for fault tolerance

## API Documentation

- **OpenAPI/Swagger**: `/swagger-ui.html`
- **Standardized Errors**: RFC 7807 Problem Details
- **Health Checks**: `/actuator/health`

## Setup & Deployment

### Prerequisites

- Java 17+
- Maven 3.9+
- Docker

### Running Locally

```bash
docker compose up --build
mvn spring-boot:run
```

### Access

- Swagger UI: `http://localhost:8003/swagger-ui.html`
- Health: `http://localhost:8003/actuator/health`

### Docker Compose Services

| Service | Port | Description |
|---------|------|-------------|
| `doctor-service` | 8003 | Doctor management service |
| `doctor-service-postgres` | 5435 | Doctor database |

## API Endpoints

### Doctor Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/doctors` | List doctors (paginated) |
| GET | `/api/v1/doctors/{id}` | Get doctor by ID |
| POST | `/api/v1/doctors` | Create doctor |
| PUT | `/api/v1/doctors/{id}` | Update doctor |

### Request/Response Examples

#### Create Doctor

```http
POST /api/v1/doctors HTTP/1.1
Content-Type: application/json

{
  "firstName": "Dr. Carlos",
  "lastName": "Rodriguez",
  "specialization": "Cardiology",
  "credentials": "MD, PhD",
  "licenseNumber": "LO-12345",
  "phone": "+573007654321",
  "email": "carlos.rodriguez@medsync.com",
  "status": "ACTIVE"
}
```

#### Response

```json
{
  "id": "uuid",
  "firstName": "Dr. Carlos",
  "lastName": "Rodriguez",
  "specialization": "Cardiology",
  "credentials": "MD, PhD",
  "licenseNumber": "LO-12345",
  "phone": "+573007654321",
  "email": "carlos.rodriguez@medsync.com",
  "status": "ACTIVE",
  "createdAt": "2026-10-06T10:00:00.000Z",
  "updatedAt": "2026-10-06T10:00:00.000Z"
}
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `DOCTOR_SERVER_PORT` | `8003` | Service port |
| `DOCTOR_SERVER_PROFILES` | `dev` | Spring profiles (dev/prod) |
| `DOCTOR_ENCRYPTION_KEY` | `<GENERATE_KEY>` | AES encryption key (32+ chars) |
| `MEDSYNC_HASH_KEY` | `<GENERATE_KEY>` | Hash key for identifiers |
| `DOCTOR_SERVER_DB_URL` | `jdbc:postgresql://localhost:5435/doctor-service` | Database URL |

### Database Migration

- **Flyway**: Automatic on startup
- **Location**: `src/main/resources/db/migration/V1__init_doctors.sql`
- **Tables**: `doctors` with encrypted columns

## Testing

### Test Coverage

- **Line Coverage**: 85% minimum (`JaCoCo` enforced, recently expanded)
- **Unit Tests**: Cover domain and integration tests
- **Integration Tests**: Testcontainers with PostgreSQL
- **Recent Updates**: Tests expanded per commit `0bd1789`

### Test Commands

```bash
mvn test
mvn jacoco:report
```

## Monitoring & Observability

### Actuator Endpoints

- `/actuator/health` - Service health
- `/actuator/info` - Service information
- `/actuator/metrics` - Application metrics

## Error Handling

### Standardized Errors (RFC 7807)

```json
{
  "type": "https://api.medsync.com/errors/validation-error",
  "title": "Validation Error",
  "status": 400,
  "detail": "Doctor license already exists",
  "instance": "/api/v1/doctors",
  "timestamp": "2026-10-06T10:00:00.000Z",
  "code": "DUPLICATE_RESOURCE"
}
```

### Error Codes

| Code | Status | Description |
|------|--------|-------------|
| `VALIDATION_ERROR` | `400` | Input validation failures |
| `DUPLICATE_RESOURCE` | `409` | License number already exists |
| `RESOURCE_NOT_FOUND` | `404` | Doctor not found |
| `INTERNAL_ERROR` | `500` | Unexpected errors |

## Future Enhancements

- **Availability**: Doctor scheduling and availability management
- **Specializations**: Advanced search by specialization and location
- **Audit Trail**: Doctor record change history
- **Integration**: Enhanced appointment service integration

## License

Part of MedSync Platform — educational and demonstration purposes.

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: 2026-10-06