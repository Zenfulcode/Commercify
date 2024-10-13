# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy the application source and build the jar
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM openjdk:17-jdk-slim
VOLUME /tmp
EXPOSE 8083

# Copy the jar file from the build stage
COPY --from=build /app/target/ProductsService.jar /app.jar

# Run the jar file
ENTRYPOINT ["java", "-jar", "/app.jar"]
