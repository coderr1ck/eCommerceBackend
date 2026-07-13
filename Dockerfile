# --- Stage 1: Build the JAR file ---
FROM maven:3.9.6-eclipse-temurin-17-alpine AS builder

WORKDIR /build

# Copy only pom.xml first
COPY pom.xml .

# Download dependencies separately to leverage true Docker layer caching
RUN --mount=type=cache,target=/root/.m2 \
    mvn dependency:go-offline -B

# Copy source code and build the package
COPY src ./src

# Build without running unit tests
RUN --mount=type=cache,target=/root/.m2 \
    mvn clean package -DskipTests -B

# --- Stage 2: Create the lightweight runtime image ---
FROM gcr.io/distroless/java17-debian13

WORKDIR /app

# Copy the specific built jar (Avoid using wildcards * to prevent build failures)
COPY --from=builder /build/target/backend-0.0.1-SNAPSHOT.jar app.jar

# Expose Spring Boot's default port
EXPOSE 8080

# Execute the application
ENTRYPOINT ["java", "-jar", "app.jar"]
