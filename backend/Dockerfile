# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

COPY src src

RUN ./mvnw clean package -DskipTests


# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

RUN apt-get update && \
    apt-get install -y clamav clamav-daemon netcat-openbsd && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

COPY docker/start.sh start.sh

RUN chmod +x start.sh

EXPOSE 8080
EXPOSE 3310

ENTRYPOINT ["./start.sh"]