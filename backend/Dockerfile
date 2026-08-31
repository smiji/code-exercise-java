# Stage 1: Build the application
FROM maven:3.9-eclipse-temurin-21-alpine AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests


# Stage 2: Create the runtime image using Java 21
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S spring && adduser -S spring -G spring
RUN mkdir -p /app/data /app/logs
COPY --from=build /app/target/*.jar app.jar

VOLUME /tmp
VOLUME /app/data
VOLUME /app/logs

EXPOSE 8585

ENTRYPOINT ["java", "-Dspring.profiles.active=docker", "-jar", "app.jar"]


