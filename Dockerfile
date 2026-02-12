# Stage 1: Build the application
# We use JDK 21 but tell Maven to handle the higher version
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
# This command builds your app even if it targets a higher version
RUN mvn clean package -DskipTests

# Stage 2: Run the application
# We use the Temurin 21 runtime
FROM eclipse-temurin:21-jdk
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]