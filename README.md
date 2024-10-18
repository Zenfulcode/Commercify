# Commercify Backend

## Description

Commercify is a backend service for a e-commerce platform. It provides APIs for user authentication, product management,
order management, and payment gateway integration. The application is built using Java and Spring Boot. The application
is designed to be modular and scalable, and can be deployed on any cloud platform.

---

## Client and Admin Frontends

The admin dashboard is built using React, TypeScript ontop of NextJS. The admin dashboard communicates with the backend
APIs to perform product management, order management, and inventory management. You can find the admin dashboard
repository [here](https://github.com/zenfulcode/commercifyweb).

## Features

- User authentication and authorization
- Product management
- Order management
- Payment gateway integration
- Inventory management (coming soon)

## Requirements

- Java 17
- Maven 3.9.9 (or higher)
- **Optional:** Docker 20.10.7
- **Optional:** Docker Compose 1.29.2

## Running the Application

To set up the application on your local machine, follow these steps:
Note: You can also use the provided Docker Compose file to run the application.

1. **Clone the repository**
   ```bash
   git clone https://github.com/gkhaavik/Commercify.git
   cd Commercify
   ```

2. **Copy the environment file and update the values**

   ```
   cp .env.example .env
   ```

3. **Start the database**
   ```bash
   docker compose -f docker-compose.db.yml up
   ```
   Note: If you don't have Docker installed, you can use a local database instead.

4. **Run the services**
   ```bash
   cd UserService && mvn spring-boot:run
   cd ProductService && mvn spring-boot:run
   cd OrderService && mvn spring-boot:run
   cd PaymentService && mvn spring-boot:run
   ```
   Note: You can run each service in a separate terminal window.

5. **Run the frontend**
6. **Access the application**
   ```
   orderservice -> http://localhost:8081
   paymentservice -> http://localhost:8082
   productservice -> http://localhost:8083
   userservice -> http://localhost:8084
   ```
   Find the postman collection for the
   APIs to test
   locally [here](https://elements.getpostman.com/redirect?entityId=15305317-a422f193-3a8c-4d1c-a52f-4f2e6a114519&entityType=collection).

### Run using Docker Compose

To run the application using Docker Compose, follow these steps:

1. **Clone the repository**
   ```bash
   git clone https://github.com/gkhaavik/Commercify.git
   cd Commercify
   ```
2. **Copy the environment file and update the values**

   ```
   cp .env.example .env
   ```
3. **Start the application**
   ```bash
   docker compose up --build
   ```
   Note: This will build the Docker images and run the application.
4. **Stop the application**
   ```bash
   docker compose down
   ```
   Note: This will stop and remove the Docker containers.
5. **Access the application**
   ```
   orderservice -> http://localhost:8081
   paymentservice -> http://localhost:8082
   productservice -> http://localhost:8083
   userservice -> http://localhost:8084
   ```
   Find the postman collection for the
   APIs to test
   locally [here](https://elements.getpostman.com/redirect?entityId=15305317-a422f193-3a8c-4d1c-a52f-4f2e6a114519&entityType=collection).

---

## Contributing

If you want to contribute to the project, you can fork the repository and make changes as per your requirements. Once
you are done with the changes, you can create a pull request to the main branch. I will review the changes and merge
them if they are good.
