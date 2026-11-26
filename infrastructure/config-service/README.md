# Config Server

Config Server provides shared and service-specific Spring configuration. It runs with Spring Cloud Config's `native` profile and reads YAML packaged in the application.

## Configuration

- [Shared configuration](src/main/resources/config/common/application.yaml) defines common settings for services, including Eureka and management endpoints.
- [Service configuration directories](src/main/resources/config/) contain application and profile settings. The server looks under `config/common` and `config/{application}` on its classpath.
- `CONFIG_SERVER_PORT` sets the listen port. The Docker Compose template uses `8888`.
- `SERVER_USER` and `SERVER_PASSWORD` configure HTTP Basic authentication. Clients supply the same credentials when loading remote configuration. `/actuator/health` is public.

## Operation

Start the service through the [platform Docker Compose setup](../../README.md#run-with-docker-compose). Compose uses `config-service:8888` on its internal network. An authenticated client can request a configuration environment using `/{application}/{profile}`, for example `/api-gateway/prod`.

Run a health check from the repository root after starting the stack.

```sh
docker compose -f deployment/docker-compose.yml --env-file deployment/env/.env.docker exec config-service curl -fsS http://localhost:8888/actuator/health
```

## Tests

The module includes a Spring application-context test. Run it from the repository root.

```sh
mvn -pl infrastructure/config-service -am test
```
