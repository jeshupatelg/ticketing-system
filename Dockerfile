# ==========================================
# Multi-stage Dockerfile for Ticketing System
# ==========================================

# Stage 1: Build React Frontend
FROM node:22-alpine AS frontend-builder
WORKDIR /app/frontend
COPY frontend/package*.json ./
RUN npm install
COPY frontend/ ./
RUN npm run build

# Stage 2: Build Spring Boot Backend
FROM maven:3.9.6-eclipse-temurin-21 AS backend-builder
WORKDIR /app
COPY pom.xml ./
# Download dependencies
RUN mvn dependency:go-offline -B
COPY src ./src
# Copy built static frontend files into Spring Boot static resources
COPY --from=frontend-builder /app/src/main/resources/static ./src/main/resources/static
RUN mvn clean package -DskipTests

# Stage 3: Production Runtime
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Create application user
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app appuser

# Create data directories with appropriate permissions
RUN mkdir -p /app/data/attachments /app/data/avatars && chown -R appuser:appgroup /app

# Copy JAR from backend-builder
COPY --from=backend-builder --chown=appuser:appgroup /app/target/ticketing-system-1.0.0.jar /app/app.jar

USER appuser

# Expose configurable default port
EXPOSE 8080

# Healthcheck
HEALTHCHECK --interval=30s --timeout=5s --start-period=30s --retries=3 \
  CMD curl -f http://localhost:8080${HOST_PREFIX:-/ticketing}/api/metrics || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
