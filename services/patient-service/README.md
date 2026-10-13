# Patient Service

**Patient Service** manages patient records with encryption, validation, and secure data handling for the MedSync Platform.

## Overview

- **Port**: 8002
- **Database**: PostgreSQL (Docker container: `patient-service-postgres`)
- **Status**: ✅ Production Ready
- **Languages**: Java 17
- **Framework**: Spring Boot 4.1.0
- **Tests**: 3 test files (85%+ coverage)

## Responsibilities

- **Patient Management**: Full CRUD operations for patient records
- **Data Encryption**: AES encryption for sensitive PII (phone, email, address)
- **Data Integrity**: SHA-256 hashing for document numbers
- **Validation**: Comprehensive input validation and uniqueness checks
- **Pagination**: Native Spring Data pagination support

## Key Features

### Patient Records

- **Fields**: First name, last name, document number, gender, birth date, phone, email, address, blood type
- **Status Management**: Active/Inactive patient states
- **Uniqueness**: Document number uniqueness enforced at database and application level

### Data Security

- **AES Encryption**: Phone, email, and address fields encrypted at rest
- **Deterministic Hashing**: Document numbers hashed with SHA-256 for lookups without exposure
- **Field-Level Protection**: Sensitive fields never exposed in API responses

### Validation

- **Input Validation**: Jakarta Bean Validation on all DTOs
- **Business Rules**: Age validation, document format, contact information format
- **Uniqueness**: Real-time duplicate detection

## Architecture

### Layer Structure

| Layer | Responsibility |
|-------|----------------|
| **Controller** | REST API endpoints (`PatientController`) |
| **Service** | Business logic (`PatientServiceImpl`, `PatientUniquenessValidator`, `PatientHashService`) |
| **Repository** | Data access (`PatientRepository`) |
| **Domain** | Entities, enums, constants, converters |
| **Mapper** | MapStruct mapping between entities and DTOs |

### Security Components

- **EncryptedStringConverter**: JPA AttributeConverter for AES encryption/decryption
- **DeterministicHasher**: SHA-256 hashing for document numbers
- **PatientHashService**: Hash computation and validation

## Technology Stack

### Backend

- Java 17
- Spring Boot 4.1.0
- Spring Data JPA
- Spring Cloud (Eureka, Config)
- Flyway (Database Migrations)
- PostgreSQL
- MapStruct 1.5.5.Final
- Jasypt 1.9.3 (property encryption)

### Security

- **Encryption**: AES-256 via JPA converters
- **Hashing**: SHA-256
- **Validation**: Jakarta Bean Validation
- **Configuration**: Jasypt for encrypted properties

### Infrastructure

- **Docker**: Multi-stage builds, healthchecks via Actuator
- **Service Discovery**: Eureka Client
- **Configuration**: Spring Cloud Config Server
- **Monitoring**: Spring Boot Actuator

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

- Swagger UI: `http://localhost:8002/swagger-ui.html`
- Health: `http://localhost:8002/actuator/health`

### Docker Compose Services

| Service | Port | Description |
|---------|------|-------------|
| `patient-service` | 8002 | Patient management service |
| `patient-service-postgres` | 5432 | Patient database |

## API Endpoints

### Patient Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/patients` | List patients (paginated, default size 20) |
| GET | `/api/v1/patients/{id}` | Get patient by ID |
| POST | `/api/v1/patients` | Create patient |
| PUT | `/api/v1/patients/{id}` | Update patient |
| PATCH | `/api/v1/patients/deactivate/{id}` | Deactivate patient |

### Request/Response Examples

#### Create Patient

```http
POST /api/v1/patients HTTP/1.1
Content-Type: application/json

{
  "firstName": "Maria",
  "lastName": "Garcia",
  "documentNumber": "12345678",
  "gender": "FEMALE",
  "birthDate": "1985-03-20",
  "phone": "+573001234567",
  "email": "maria@example.com",
  "address": "Calle 123 #45-67",
  "bloodType": "A_POSITIVE"
}
```

#### Response

```json
{
  "id": "uuid",
  "firstName": "Maria",
  "lastName": "Garcia",
  "documentNumber": "12345678",
  "gender": "FEMALE",
  "birthDate": "1985-03-20",
  "phone": "+573001234567",
  "email": "maria@example.com",
  "address": "Calle 123 #45-67",
  "bloodType": "A_POSITIVE",
  "status": "ACTIVE",
  "createdAt": "2026-10-06T10:00:00.000Z",
  "updatedAt": "2026-10-06T10:00:00.000Z"
}
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `PATIENT_SERVER_PORT` | `8002` | Service port |
| `PATIENT_SERVER_PROFILES` | `dev` | Spring profiles (dev/prod) |
| `PATIENT_ENCRYPTION_KEY` | `<GENERATE_KEY>` | AES encryption key (32+ chars) |
| `MEDSYNC_HASH_KEY` | `<GENERATE_KEY>` | Hash key for document numbers |
| `PATIENT_SERVER_DB_URL` | `jdbc:postgresql://localhost:5432/patient-service` | Database URL |

### Database Migration

- **Flyway**: Automatic on startup
- **Location**: `src/main/resources/db/migration/V1__init_patients.sql`
- **Tables**: `patients` with encrypted columns

## Testing

### Test Coverage

- **Line Coverage**: 85% minimum (`JaCoCo` enforced)
- **Unit Tests**: 3 files covering services, validators, converters
- **Integration Tests**: Testcontainers with PostgreSQL
- **Validation Tests**: DTO and business rule validation

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
  "detail": "Document number already exists",
  "instance": "/api/v1/patients",
  "timestamp": "2026-10-06T10:00:00.000Z",
  "code": "DUPLICATE_RESOURCE"
}
```

### Error Codes

| Code | Status | Description |
|------|--------|-------------|
| `VALIDATION_ERROR` | `400` | Input validation failures |
| `DUPLICATE_RESOURCE` | `409` | Document number already exists |
| `RESOURCE_NOT_FOUND` | `404` | Patient not found |
| `INTERNAL_ERROR` | `500` | Unexpected errors |

## Future Enhancements

- **Soft Delete**: Archive instead of hard delete
- **Advanced Search**: Full-text search capabilities
- **Audit Trail**: Patient record change history
- **Bulk Operations**: Import/export patient data

## License

Part of MedSync Platform — educational and demonstration purposes.

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: 2026-10-06