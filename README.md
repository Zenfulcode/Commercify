# Commercify

Commercify is a Spring Boot-based e-commerce backend application that provides a robust API for managing products,
orders, and payments. It includes user authentication, order management, and integration with Stripe for payment
processing.

## Features

- User authentication and authorization with JWT
- Product management
- Order management system
- Payment processing with Mobilepay and Stripe integration(coming soon)
- Docker support for easy deployment
- Database migrations with Liquibase

## Prerequisites

- Java 17
- Maven 3.9.x
- Docker and Docker Compose
- MySQL 8.0 or later
- MobilePay Vipps account
- Stripe account for payment processing (Coming soon)

## Tech Stack

- Spring Boot 3.3.5
- Spring Security
- Spring Data JPA
- MySQL
- Liquibase
- JWT Authentication
- Stripe Payment Integration
- Docker
- Maven

## Getting Started

### Environment Setup

1. Clone the repository
2. Create a `.env` file in the `deploy` directory: `cp .env.example .env`

### Running with Docker

Use the provided Makefile commands:

```bash
# Start the application
make up

# Stop the application
make down

# Rebuild and restart the application
make update
```

### Running Locally

1. Start MySQL database
2. Build the project:

```bash
mvn clean package
```

3. Run the application:

```bash
java -jar target/Commercify.jar
```

#### Alternative: Using Spring Boot Maven Plugin

Still need to start MySQL database first.

```bash
mvn spring-boot:run
```

## API Endpoints

View and test the API endpoints in our Postman Workspace.

[![Run in Postman](https://run.pstmn.io/button.svg)](https://www.postman.com/zenfulcode/workspace/commercify-rest-api)

## Security

The application uses JWT (JSON Web Token) for authentication. Protected endpoints require a valid JWT token in the
Authorization header:

```
Authorization: Bearer <your_jwt_token>
```

## Docker Support

The application includes a multi-stage Dockerfile for optimized builds and a docker-compose configuration for easy
deployment. The Docker setup includes:

- Maven build stage
- JRE runtime stage
- Automatic environment variable configuration
- Port mapping (6091)
- Volume mapping for Liquibase changelog files

## Development

### Adding New Features

1. Create feature branch
2. Implement changes
3. Add tests
4. Create pull request

### Testing

Run the tests using Maven:

```bash
mvn test
```

## Contributing

1. Fork the repository
2. Create your feature branch
3. Commit your changes
4. Push to the branch
5. Create a new Pull Request

## Support

For support, email commercify@zenfulcode.com
