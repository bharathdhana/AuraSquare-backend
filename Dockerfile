# =========================================================
# Stage 1: Build Stage
# =========================================================
FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /app

# Copy Maven wrapper and pom.xml first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

# Download dependencies offline for faster incremental builds
RUN ./mvnw dependency:go-offline -B || true

# Copy source code and build the application JAR
COPY src ./src
RUN ./mvnw clean package -DskipTests

# =========================================================
# Stage 2: Runtime Stage
# =========================================================
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Create a non-root security user and group
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

# Copy compiled JAR from builder stage
COPY --from=builder /app/target/*.jar app.jar

# Set permissions to non-root user
RUN chown -R appuser:appgroup /app
USER appuser

# Expose backend service port
EXPOSE 8080

# Configure runtime options
ENV JAVA_OPTS="-Xms256m -Xmx512m -XX:+UseG1GC"

# Launch Spring Boot Application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -Djava.security.egd=file:/dev/./urandom -jar app.jar"]
