# Appointment Service

**Appointment Service** manages appointment scheduling between patients and doctors with resilience patterns, inter-service communication, and health tracking.

## Overview

- **Port**: 8004
- **Database**: PostgreSQL (Docker container: `appointment-service-postgres`)
- **Status**: 🟡 In Development (functional core ready)
- **Languages**: Java 17
- **Framework**: Spring Boot 4.1.0
- **Tests**: 3 test files

## Responsibilities

- **Appointment Management**: Schedule, list, and manage patient-doctor appointments
- **Schedule Validation**: Prevent overlapping appointments for the same doctor
- **Inter-service Communication**: Fetch patient and doctor details via OpenFeign
- **Resilience**: Circuit breaker and fallback patterns for fault tolerance

## Key Features

### Appointment Management

- **Fields**: Patient ID, Doctor ID, appointment datetime, status, notes
- **Status Workflow**: SCHEDULED → CONFIRMED/CANCELLED/COMPLETED
- **Validation**: Doctor availability, date/time constraints

### Inter-service Communication

| Client | Target | Purpose |
|--------|--------|---------|
| `DoctorClient` | Doctor Service (8003) | Verify doctor exists |
| `PatientClient` | Patient Service (8002) | Fetch patient details |

### Resilience Patterns

- **Circuit Breaker**: Resilience4j wraps Feign calls to prevent cascading failures
- **Fallbacks**: `DoctorClientFallback` and `PatientClientFallback` return default/partial data
- **Retry Logic**: Configurable retry with exponential backoff

### Security

- **JWT Protected**: All endpoints require valid Bearer token
- **Feign Interceptor**: Attaches JWT to outgoing inter-service calls

## Architecture

### Layer Structure

| Layer | Responsibility |
|-------|----------------|
| **Controller** | REST API endpoints (`AppointmentController`) |
| **Service** | Business logic (`AppointmentServiceImpl`) |
| **Repository** | Data access (`AppointmentRepository`) |
| **Domain** | Entity, scheduler validator (`AppointmentScheduleValidator`) |
| **Client** | OpenFeign clients + fallbacks |
| **Exception** | Custom exception handling (`GlobalExceptionHandler`) |

### Communication Flow

```
Client → Appointment Service → Feign → Doctor Service
                                → Feign → Patient Service
                                → PostgreSQL (local DB)
```

## Technology Stack

### Backend

- Java 17
- Spring Boot 4.1.0
- Spring Data JPA
- Spring Cloud (Eureka, Config, OpenFeign)
- Spring Cloud CircuitBreaker (Resilience4j)
- Flyway
- PostgreSQL

### Resilience

- **Resilience4j**: Circuit breaker for Feign clients
- **Fallbacks**: Default responses on service failure

### Infrastructure

- **Docker**: Multi-stage builds
- **Service Discovery**: Eureka Client
- **Configuration**: Spring Cloud Config Server

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

- Swagger UI: `http://localhost:8004/swagger-ui.html`
- Health: `http://localhost:8004/actuator/health`

### Docker Compose Services

| Service | Port | Description |
|---------|------|-------------|
| `appointment-service` | 8004 | Appointment scheduling service |
| `appointment-service-postgres` | 5434 | Appointment database |

### Dependencies (Docker)

The service depends on:
- `appointment-service-postgres` (database)
- `config-service` (configuration)
- `discovery-service` (service registry)
- `auth-service` (authentication)
- `patient-service` (patient data)
- `doctor-service` (doctor data)

## API Endpoints

### Appointment Management

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/appointments` | List appointments |
| GET | `/api/v1/appointments/{id}` | Get appointment by ID |
| POST | `/api/v1/appointments` | Create appointment |
| PUT | `/api/v1/appointments/{id}` | Update appointment |

### Request/Response Examples

#### Create Appointment

```http
POST /api/v1/appointments HTTP/1.1
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "patientId": "uuid-patient",
  "doctorId": "uuid-doctor",
  "appointmentDateTime": "2026-10-15T10:30:00",
  "durationMinutes": 30,
  "reason": "Routine checkup"
}
```

#### Response

```json
{
  "id": "uuid-appointment",
  "patientId": "uuid-patient",
  "doctorId": "uuid-doctor",
  "appointmentDateTime": "2026-10-15T10:30:00",
  "durationMinutes": 30,
  "status": "SCHEDULED",
  "reason": "Routine checkup",
  "createdAt": "2026-10-06T10:00:00.000Z"
}
```

## Configuration

### Environment Variables

| Variable | Default | Description |
|----------|---------|-------------|
| `APPOINTMENT_SERVER_PORT` | `8004` | Service port |
| `APPOINTMENT_SERVER_PROFILES` | `dev` | Spring profiles |
| `APPOINTMENT_SERVER_DB_URL` | `jdbc:postgresql://localhost:5434/appointment-service` | Database URL |
| `AUTH_SERVER_SECRET_KEY` | — | JWT validation key |

## Testing

### Test Coverage

- **Line Coverage**: 85% minimum (`JaCoCo` enforced)
- **Unit Tests**: 1 file (`AppointmentServiceImplTest`)
- **Integration Tests**: 1 file (`AppointmentControllerIntegrationTest`)
- **Error Handling Tests**: 1 file (`GlobalExceptionHandlerTest`)

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
- `/actuator/circuitbreakers` - Circuit breaker status

### Circuit Breaker Monitoring

Resilience4j circuit breaker state is visible via:
- `/actuator/circuitbreakers`
- Health endpoint includes circuit breaker status

## Error Handling

### Standardized Errors (RFC 7807)

```json
{
  "type": "https://api.medsync.com/errors/conflict",
  "title": "Conflict",
  "status": 409,
  "detail": "Doctor already has an appointment at 10:30",
  "instance": "/api/v1/appointments",
  "timestamp": "2026-10-06T10:00:00.000Z",
  "code": "CONFLICT"
}
```

### Error Codes

| Code | Status | Description |
|------|--------|-------------|
| `VALIDATION_ERROR` | `400` | Input validation failures |
| `RESOURCE_NOT_FOUND` | `404` | Appointment/doctor/patient not found |
| `CONFLICT` | `409` | Schedule conflict |
| `SERVICE_UNAVAILABLE` | `503` | Downstream service unavailable (fallback triggered) |

## Inter-service Communication

### Feign Clients

```java
@FeignClient(name = "doctor-service")
public interface DoctorClient {
    @GetMapping("/api/v1/doctors/{id}")
    DoctorResponse getDoctorById(@PathVariable("id") UUID id);
}
```

### Fallback Example

```java
@Component
public class DoctorClientFallback implements DoctorClient {
    @Override
    public DoctorResponse getDoctorById(UUID id) {
        return new DoctorResponse("unknown", "Unavailable");
    }
}
```

## Future Enhancements

- **Notifications**: Reminders via email/SMS
- **Recurring appointments**: Weekly/monthly schedules
- **Cancellation policy**: Time-based cancellation rules
- **Audit log**: Appointment history tracking
- **Availability management**: Doctor calendar integration

## License

Part of MedSync Platform — educational and demonstration purposes.

---

**Version**: 0.0.1-SNAPSHOT  
**Last Updated**: 2026-10-06