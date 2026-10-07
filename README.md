# RideShare Backend

A scalable ride-sharing backend built with Java, Spring Boot, Spring Data JPA, PostgreSQL, and REST APIs.

## Tech Stack

- Java 21
- Spring Boot
- Spring Data JPA / Hibernate
- PostgreSQL
- Maven
- REST APIs
- Jakarta Bean Validation

## Current Features

### User Management

- User CRUD APIs
- Full user updates using `PUT`
- Partial user updates using `PATCH`
- DTO-based request and response handling
- Layered architecture (Controller → Service → Repository)
- PostgreSQL persistence with Spring Data JPA
- Application-level and database-level email uniqueness
- User roles (`RIDER`, `DRIVER`)
- User search by name and email
- Pagination and sorting

### Driver & Vehicle Domain

- Driver entity linked to User
- Driver availability status (`AVAILABLE`, `BUSY`, `OFFLINE`)
- Vehicle entity with unique registration number
- Vehicle type classification
- Driver-to-Vehicle current assignment
- Database-level foreign key and uniqueness constraints

### Validation & Error Handling

- Request validation using Jakarta Bean Validation
- Required-field validation for name, email, and phone
- Email format validation
- 10-digit phone number validation
- Custom validation messages
- Global exception handling
- Structured API error responses
- Multiple field-level validation errors
- Handling for `400`, `404`, and `409` error scenarios
- Database constraint violation handling

## Architecture

The backend follows a layered architecture:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
PostgreSQL
```

The domain model currently includes:

```text
User
 └── Driver
      └── Vehicle
```

A `User` can have the role `RIDER` or `DRIVER`. A Driver contains driver-specific information such as license number and availability status, while Vehicle is modeled independently and can be reassigned to another driver later.

## Project Status

🚧 Currently under development.

**Completed:**
- User management module
- CRUD operations
- DTO-based API design
- Input validation
- Global exception handling
- Search, pagination, and sorting
- Application-level and database-level email uniqueness
- User roles
- Driver and Vehicle domain entities
- Driver availability status
- Driver-Vehicle current assignment
- PostgreSQL relationships and constraints

**Current Focus:** Building the core RideShare domain and ride lifecycle.

## Planned Features

- Ride management
- Ride request and matching workflow
- Driver acceptance/rejection
- Ride cancellation
- Ride start and completion
- Fare calculation
- Ride history
- Database indexing and transaction management
- Spring Security & JWT authentication
- Redis caching
- Unit and integration testing
- Testcontainers with PostgreSQL
- Docker containerization
- API Gateway
- Service discovery
- Microservices architecture
- Centralized logging and monitoring
- Swagger / OpenAPI documentation
- Cloud deployment
- CI/CD pipeline