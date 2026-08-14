# Multi-stage Dockerfile for discovery-service
# Stage 1: Build with Maven (optimized caching)
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy only pom.xml files first (cacheable layer)
COPY pom.xml .
COPY discovery-service/pom.xml discovery-service/
COPY auth-service/pom.xml auth-service/
COPY config-service/pom.xml config-service/
COPY api-gateway/pom.xml api-gateway/
COPY patient-service/pom.xml patient-service/
COPY common-security/pom.xml common-security/
COPY common-core/pom.xml common-core/

# Pre-download dependencies (cache layer, reused if pom.xml unchanged)
RUN mvn -f /app/pom.xml dependency:resolve -pl discovery-service -am -q || true

# Copy source code AFTER dependencies (only invalidates cache on source change)
COPY discovery-service/src discovery-service/src

# Build (uses cached dependencies)
RUN mvn -f /app/pom.xml clean package -pl discovery-service -am -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/discovery-service/target/discovery-service-*.jar discovery-service.jar

EXPOSE 8761

ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8761/actuator/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar discovery-service.jar"]
