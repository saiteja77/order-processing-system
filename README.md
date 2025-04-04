# Order Processing System

A reactive microservice-based application that simulates an order processing system using Spring Boot, WebFlux, Kafka, and R2DBC.

## Architecture

The application consists of two main microservices:

1. **Order Service**: Manages order creation, updates, and retrieval
2. **Notification Service**: Sends notifications when orders are created or updated

### Communication Flow

- Order Service creates/updates orders in the H2 database
- Order Service publishes order events to Kafka
- Notification Service consumes order events from Kafka and processes notifications

## Technologies Used

- **Java 21**: Core programming language
- **Spring Boot 3.2.5**: Application framework
- **Spring WebFlux**: Reactive web framework
- **Project Reactor**: Reactive programming library
- **R2DBC**: Reactive database connectivity
- **H2 Database**: In-memory database
- **Apache Kafka**: Message broker for event-driven architecture
- **Gradle**: Build tool
- **Docker**: Containerization
- **JUnit 5 & Mockito**: Testing

## Project Structure

The project follows a multi-module Gradle structure:

- **common**: Shared models and exceptions
- **order-service**: Order management functionality
- **notification-service**: Notification handling functionality

## API Endpoints

### Order Service (Port 8080)

- **POST /orders**: Create a new order
    - Request Body: `{ "customerName": "First Last", "totalAmount": 100.0, "description": "My order" }`

- **GET /orders/{id}**: Retrieve an order by ID

- **PUT /orders/{id}**: Update an existing order
    - Request Body: `{ "customerName": "FirstName LastName", "totalAmount": 150.0, "description": "Updated order" }`

### Notification Service (Port 8081)

- **GET /notifications**: Fetches all the orders from the kafka History

## Error Handling

The application implements comprehensive error handling:

- Global exception handlers for both services
- Retry mechanisms for Kafka operations
- Validation for request payloads
- Appropriate HTTP status codes for different error scenarios
- Detailed error responses with timestamps and path information

## Running the Application

### Prerequisites

- Docker and Docker Compose to be installed in the machine

### Steps

1. Build and start the services:

```bash
docker-compose up --build
```

2. Services will be available at:
    - Order Service: http://localhost:8080
    - Notification Service: http://localhost:8081

## Testing

The application includes comprehensive unit tests for controllers and services.

To run the tests, got to the root project folder order-processing-system and run the command:

```bash
./gradlew test
```

## Monitoring

The application exposes the following Actuator endpoints for each of the two services:

- Health: `/actuator/health`
- Info: `/actuator/info`
- Metrics: `/actuator/metrics`
- Prometheus: `/actuator/prometheus`

## Future Enhancements

- Move out from In memory DataBase to realtime DataBase
- Add API Gateway
- Add service discovery
- Implement circuit breakers
- Add authentication/authorization
- Deploy to Kubernetes