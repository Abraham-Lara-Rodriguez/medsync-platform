# Multi-stage Dockerfile for patient-service
# Stage 1: Build with Maven (optimized caching)
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy only pom.xml files first (cacheable layer)
COPY pom.xml .
COPY patient-service/pom.xml patient-service/
COPY config-service/pom.xml config-service/
COPY discovery-service/pom.xml discovery-service/
COPY api-gateway/pom.xml api-gateway/
COPY auth-service/pom.xml auth-service/
COPY common-security/pom.xml common-security/
COPY common-core/pom.xml common-core/

# Pre-download dependencies (cache layer, reused if pom.xml unchanged)
RUN mvn -f /app/pom.xml dependency:resolve -pl patient-service -am -q || true

# Copy source code AFTER dependencies (only invalidates cache on source change)
COPY common-core/src common-core/src
COPY common-security/src common-security/src
COPY patient-service/src patient-service/src

# Build (uses cached dependencies)
RUN mvn -f /app/pom.xml clean package -pl patient-service -am -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/patient-service/target/patient-service-*.jar patient-service.jar

EXPOSE 8002

ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8002/actuator/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar patient-service.jar"]
