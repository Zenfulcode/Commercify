# Stage 1: Build the application
FROM maven:3.9.9-eclipse-temurin-17-alpine AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Stage 2: Create the runtime image
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV SPRING_DATASOURCE_URL=jdbc:mysql://host.docker.internal:3306/paymentdb
ENV SPRING_DATASOURCE_USERNAME=root
ENV SPRING_DATASOURCE_PASSWORD=changeme
ENV STRIPE_TEST_SECRET=changeme
ENV JWT_SECRET_KEY=changeme
ENV ORDER_SERVICE_URL=http://order-service:8081/api/v1/orders
ENV USER_SERVICE_URL=http://user-service:8084/api/v1/users

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]