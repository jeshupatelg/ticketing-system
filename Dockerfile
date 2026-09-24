FROM eclipse-temurin:21-jre-jammy
WORKDIR /app

# Install curl for healthcheck
RUN apt-get update && apt-get install -y --no-install-recommends curl && rm -rf /var/lib/apt/lists/*

# Create application user and runtime directories
RUN groupadd -r appgroup && useradd -r -g appgroup -d /app appuser
RUN mkdir -p /app/data/attachments /app/data/avatars && chown -R appuser:appgroup /app

# Copy executable Spring Boot JAR with packaged frontend
COPY --chown=appuser:appgroup target/ticketing-system-1.0.0.jar /app/app.jar

USER appuser

EXPOSE 8080

HEALTHCHECK --interval=15s --timeout=5s --start-period=20s --retries=3 \
  CMD curl -f http://localhost:8080${HOST_PREFIX:-/ticketing}/api/metrics || exit 1

ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-jar", "/app/app.jar"]
