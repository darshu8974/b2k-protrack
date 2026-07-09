# syntax=docker/dockerfile:1

# ─────────────────────────────── Build stage ───────────────────────────────
# Full JDK 21 to compile and assemble the executable Spring Boot jar.
FROM eclipse-temurin:21-jdk AS build
WORKDIR /workspace

# Copy the Gradle wrapper and build configuration first so the dependency-download
# layer stays cached until the build files themselves change.
COPY gradlew settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null 2>&1 || true

# Copy sources and build the fat jar. Tests run in CI (ci-api), not in the image build.
COPY src ./src
RUN ./gradlew --no-daemon clean bootJar -x test

# ────────────────────────────── Runtime stage ──────────────────────────────
# JRE-only image for a smaller, safer runtime.
FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

# Run as an unprivileged user.
RUN groupadd --system protrack && useradd --system --gid protrack --home /app protrack

# The single executable jar produced by bootJar.
COPY --from=build /workspace/build/libs/*.jar app.jar

# Default local file-storage location (PROTRACK_STORAGE_LOCAL_PATH); writable by the app user.
RUN mkdir -p /app/local-storage && chown -R protrack:protrack /app
USER protrack

# Render injects $PORT; Spring reads server.port=${PORT:8080}. EXPOSE documents the local default.
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75.0"
CMD ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
