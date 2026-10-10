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

# Copy source code and documentation
COPY src src
COPY README.md README.md
COPY docs docs

# Build application
RUN ./mvnw clean package -DskipTests


# ---------- Runtime Stage ----------
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy application JAR
COPY --from=builder /app/target/*.jar app.jar

# Copy only the documentation required for RAG ingestion
COPY --from=builder /app/README.md /app/rag-docs/README.md
COPY --from=builder /app/docs/ /app/rag-docs/docs/

EXPOSE 8080

ENTRYPOINT ["java", \
"-XX:+UseContainerSupport", \
"-XX:MaxRAMPercentage=75.0", \
"-Djava.security.egd=file:/dev/./urandom", \
"-jar", \
"app.jar"]