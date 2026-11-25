# Discovery Service

Discovery Service runs the Eureka registry. The business services register their instances, and clients such as API Gateway use the registry to resolve service names.

## Configuration

- Discovery imports its settings from [Config Server](../config-service/README.md) using `CONFIG_SERVER_HOSTNAME`, `CONFIG_SERVER_PORT`, `SERVER_USER`, and `SERVER_PASSWORD`.
- `DISCOVERY_SERVER_HOSTNAME` and `DISCOVERY_SERVER_PORT` define the advertised hostname and listen port. The Docker Compose template uses `discovery-service:8761`.
- The [Eureka server configuration](../config-service/src/main/resources/config/discovery-service/discovery-service.yaml) runs a single standalone registry. The server's own registration and registry fetching are disabled.
- The registry API at `/eureka/` and the Eureka dashboard require HTTP Basic authentication. `/actuator/health` is public.

## Operation

Start the service through the [platform Docker Compose setup](../../README.md#run-with-docker-compose). Compose keeps the registry on the internal network. Run this health check from the repository root.

```sh
docker compose -f deployment/docker-compose.yml --env-file deployment/env/.env.docker exec discovery-service curl -fsS http://localhost:8761/actuator/health
```

## Tests

The module includes a Spring application-context test. Run it from the repository root.

```sh
mvn -pl infrastructure/discovery-service -am test
```
