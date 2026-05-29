# Step 1: Build the Maven project using Java 21
FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

# Step 2: Run the Spring Boot application binding to all network interfaces
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 10000
ENTRYPOINT ["java", "-Dserver.port=10000", "-Dserver.address=0.0.0.0", "-jar", "app.jar"]