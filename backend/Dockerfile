# ---------- Build Stage ----------
FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Maven configuration
COPY backend/.mvn .mvn
COPY backend/mvnw .
COPY backend/pom.xml .

# Download dependencies
RUN chmod +x mvnw
RUN ./mvnw dependency:go-offline

# Backend source
COPY backend/src src

# Repository documentation
COPY README.md README.md
COPY docs docs

# Build application
RUN ./mvnw clean package -DskipTests


# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Application JAR
COPY --from=builder /app/target/*.jar app.jar

# RAG documentation
COPY --from=builder /app/README.md /app/rag-docs/README.md
COPY --from=builder /app/docs/ /app/rag-docs/docs/

EXPOSE 8080

ENTRYPOINT ["java", \
"-XX:+UseContainerSupport", \
"-XX:MaxRAMPercentage=75.0", \
"-Djava.security.egd=file:/dev/./urandom", \
"-jar", \
"app.jar"]