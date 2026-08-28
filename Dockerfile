# Multi-stage Dockerfile for Spring Boot Application

# Stage 1: Build stage
FROM eclipse-temurin:17-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and POM for dependency caching
COPY demo/.mvn/ demo/.mvn/
COPY demo/mvnw demo/pom.xml ./demo/
WORKDIR /app/demo
RUN chmod +x mvnw && ./mvnw dependency:resolve -B

# Copy source code and build package
COPY demo/src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Lightweight runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# Copy packaged jar from build stage
COPY --from=builder /app/demo/target/*.jar app.jar

# Expose Spring Boot port
EXPOSE 8080

# Run Spring Boot application
ENTRYPOINT ["java", "-jar", "app.jar"]
