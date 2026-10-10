ARG BASE_IMAGE=customized-eclipse-temurin:21-jdk

# Multi-stage build
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /build

# Copy pom.xml and source code
COPY pom.xml .
COPY src ./src

# Build production Spring Boot executable JAR
RUN mvn clean package -DskipTests

# Runtime stage
FROM ${BASE_IMAGE}
WORKDIR /app

# Ensure curl is available for healthcheck if not already provided by base image
RUN which curl > /dev/null 2>&1 || (apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*)

# Patch base image script to prevent set -e termination on post-increment when count is 0
RUN sed -i 's/((loaded_count++))/(( ++loaded_count ))/' /usr/local/bin/scripts/config-import-script.sh 2>/dev/null || true

# Create application user and runtime directories
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app appuser
RUN mkdir -p /app/data/attachments /app/data/avatars && chown -R appuser:appgroup /app

# Copy executable Spring Boot JAR from builder stage
COPY --from=builder --chown=appuser:appgroup /build/target/*.jar /app/app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -f "http://localhost:8080${CONTEXT_PATH}/health" || curl -f "http://localhost:8080${CONTEXT_PATH}/index.html" || exit 1

CMD ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]

