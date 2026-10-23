# Stage 1: Build the backend JAR
FROM eclipse-temurin:25-jdk AS builder
WORKDIR /app
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x ./mvnw
RUN ./mvnw dependency:go-offline -B || true
COPY src/ ./src/
RUN ./mvnw clean package -DskipTests -B

# Stage 2: Runtime container
FROM eclipse-temurin:25-jre
WORKDIR /app

# Create directories for persistent storage and file uploads
RUN mkdir -p data storage/attachments storage/academics

# Copy the built JAR from the builder stage
COPY --from=builder /app/target/backend-0.0.1-SNAPSHOT.jar app.jar

ENV SERVER_PORT=8081
ENV SPRING_PROFILES_ACTIVE=dev

EXPOSE 8081

ENTRYPOINT ["java", "-jar", "app.jar"]
