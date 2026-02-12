# Stage 1: Build the application
FROM maven:3.9.6-eclipse-temurin-21 AS build
COPY . .
# We add -Dmaven.test.skip=true to stop it from looking at test files
RUN mvn clean package -DskipTests -Dmaven.test.skip=true

# Stage 2: Run the application
FROM eclipse-temurin:21-jdk
COPY --from=build /target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]