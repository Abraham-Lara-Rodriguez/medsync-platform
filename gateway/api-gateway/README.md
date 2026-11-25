# API Gateway

API Gateway is the HTTP entry point for MedSync. It uses Spring Cloud Gateway WebFlux and resolves its `lb://` destinations through Spring Cloud LoadBalancer and Eureka. The business services validate bearer tokens and enforce endpoint authorities.

## Routes

| Request path | Destination |
| --- | --- |
| `/api/v1/auth/**`, `/api/v1/users/**` | [Auth Service](../../services/auth-service/README.md) |
| `/api/v1/patients/**` | [Patient Service](../../services/patient-service/README.md) |
| `/api/v1/doctors/**` | [Doctor Service](../../services/doctor-service/README.md) |
| `/api/v1/appointments/**` | [Appointment Service](../../services/appointment-service/README.md) |

The [route definitions](../../infrastructure/config-service/src/main/resources/config/api-gateway/api-gateway.yaml) are served by Config Server. The gateway loads configuration using `CONFIG_SERVER_HOSTNAME`, `CONFIG_SERVER_PORT`, `SERVER_USER`, and `SERVER_PASSWORD`. Set `GATEWAY_SERVER_PROFILES` for the deployment profile and `GATEWAY_SERVER_PORT` for the listen port.

## Operation

Start the service through the [platform Docker Compose setup](../../README.md#run-with-docker-compose). Compose publishes port `8222`.

```powershell
curl.exe -fsS http://localhost:8222/actuator/health
```

Use the [flow runner](../../deployment/script/README.md) to verify routing and downstream responses through the deployed gateway.

## Tests

The module includes a Spring application-context test. Run it from the repository root.

```sh
mvn -pl gateway/api-gateway -am test
```
