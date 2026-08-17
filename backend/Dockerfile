# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven Wrapper
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Download dependencies (improves build caching)
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Copy source code
COPY src src

# Build application
RUN ./mvnw clean package -DskipTests


# ---------- Runtime Stage ----------
# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

RUN apt-get update && \
    apt-get install -y clamav clamav-daemon && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

COPY docker/start.sh start.sh

RUN chmod +x start.sh

EXPOSE 8080
EXPOSE 3310

ENTRYPOINT ["./start.sh"]