# Multi-stage Dockerfile for auth-service
# Stage 1: Build with Maven (optimized caching)
FROM maven:3.9.9-eclipse-temurin-17 AS builder

WORKDIR /app

# Copy only pom.xml files first (cacheable layer)
COPY pom.xml .
COPY services/auth-service/pom.xml services/auth-service/
COPY infrastructure/config-service/pom.xml infrastructure/config-service/
COPY infrastructure/discovery-service/pom.xml infrastructure/discovery-service/
COPY gateway/api-gateway/pom.xml gateway/api-gateway/
COPY services/patient-service/pom.xml services/patient-service/
COPY shared/common-security/pom.xml shared/common-security/
COPY shared/common-core/pom.xml shared/common-core/
COPY services/appointment-service/pom.xml services/appointment-service/

# Pre-download dependencies (cache layer, reused if pom.xml unchanged)
RUN mvn -f /app/pom.xml dependency:resolve -pl services/auth-service -am -q || true

# Copy source code AFTER dependencies
COPY shared/common-core/src shared/common-core/src
COPY services/auth-service/src services/auth-service/src

# Build
RUN mvn -f /app/pom.xml clean package -pl services/auth-service -am -DskipTests -q

# Stage 2: Runtime
FROM eclipse-temurin:17-jre

WORKDIR /app

COPY --from=builder /app/services/auth-service/target/auth-service-*.jar auth-service.jar

EXPOSE 8001

ENV JAVA_OPTS=""

HEALTHCHECK --interval=30s --timeout=10s --start-period=40s --retries=3 \
  CMD wget -qO- http://localhost:8001/actuator/health >/dev/null 2>&1 || exit 1

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar auth-service.jar"]