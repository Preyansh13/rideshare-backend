# RideShare Backend

A ride-sharing backend built with **Java, Spring Boot, Spring Data JPA, PostgreSQL, and REST APIs**. The project follows a layered architecture and focuses on building a scalable foundation for ride management, driver assignment, and the ride lifecycle.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot
- **Persistence:** Spring Data JPA / Hibernate
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **API Design:** REST APIs
- **Validation:** Jakarta Bean Validation
- **Testing Tools:** Postman
- **Development Environment:** IntelliJ IDEA

## Current Features

### 1. User Management

- User CRUD APIs
- Full updates using `PUT`
- Partial updates using `PATCH`
- DTO-based request and response handling
- Layered architecture: Controller → Service → Repository
- PostgreSQL persistence using Spring Data JPA
- Application-level and database-level email uniqueness
- User roles: `RIDER` and `DRIVER`
- User search by name and email
- Pagination and sorting

### 2. Driver & Vehicle Domain

- Driver entity linked to User
- Driver availability statuses: `AVAILABLE`, `BUSY`, `OFFLINE`
- Vehicle entity with a unique registration number
- Vehicle type classification: `HATCHBACK`, `SEDAN`, `SUV`, `MUV`
- Driver-to-Vehicle assignment relationship
- Database-level foreign key and uniqueness constraints

### 3. Ride Management

- Ride entity with rider and optional driver relationships
- Ride status management using an enum
- Automatic ride request timestamp
- Create ride API
- Retrieve ride by ID
- Retrieve all rides
- Retrieve rides by rider ID
- Filter rides by status
- DTO-based ride responses
- Custom exception handling for rides that do not exist

### 4. Validation & Error Handling

- Request validation using Jakarta Bean Validation
- Required-field validation for name, email, and phone
- Email format validation
- 10-digit phone number validation
- Custom validation messages
- Global exception handling
- Structured API error responses
- Multiple field-level validation errors
- Handling for `400 Bad Request`, `404 Not Found`, and `409 Conflict`
- Database constraint violation handling

## API Endpoints

The following endpoints have been implemented as part of the project so far.

### User APIs

| Method | Endpoint | Purpose |
|---|---|---|
| `GET` | `/api/users` | Retrieve users with pagination and sorting |
| `GET` | `/api/users/{id}` | Retrieve a user by ID |
| `POST` | `/api/users` | Create a user |
| `PUT` | `/api/users/{id}` | Fully update a user |
| `PATCH` | `/api/users/{id}` | Partially update a user |
| `DELETE` | `/api/users/{id}` | Delete a user |
| `GET` | `/api/users/search` | Search users by name or email |

### Ride APIs

| Method | Endpoint | Purpose |
|---|---|---|
| `POST` | `/api/rides` | Create a ride request |
| `GET` | `/api/rides/{id}` | Retrieve a ride by ID |
| `GET` | `/api/rides` | Retrieve all rides |
| `GET` | `/api/rides/rider/{riderId}` | Retrieve rides belonging to a rider |
| `GET` | `/api/rides/status/{status}` | Filter rides by status |

**Example requests:**

```http
GET /api/rides
GET /api/rides/1
GET /api/rides/rider/8
GET /api/rides/status/REQUESTED
```

> Note: The endpoints above reflect the current implementation progress. Driver-based ride filtering and additional ride lifecycle operations are still planned.

## Architecture

The application currently uses a **layered monolithic architecture**.

```text
Client / Postman
       |
       v
   Controller
       |
       v
     Service
       |
       v
   Repository
       |
       v
   PostgreSQL
```

### Domain Model

```text
User
 ├── Rider
 └── Driver
       |
       v
     Vehicle

Ride
 ├── Rider (User)
 └── Driver (optional)
```

- A `User` can have the role `RIDER` or `DRIVER`.
- A `Driver` stores driver-specific information, including a license number and availability status.
- A `Vehicle` stores vehicle information and can be reassigned according to the domain rules.
- A `Ride` represents a ride request associated with a rider and, optionally, an assigned driver.

The project is **not yet a microservices architecture**. Microservices, API Gateway, and service discovery are future architectural enhancements.

## Project Status

🚧 **Under active development**

### Completed

- User management and CRUD operations
- DTO-based API design
- Input validation and global exception handling
- Search, pagination, and sorting
- Application-level and database-level email uniqueness
- User roles and driver availability status
- Driver and Vehicle domain entities
- PostgreSQL relationships and constraints
- Ride entity and status model
- Create ride API
- Retrieve ride by ID and retrieve all rides
- Retrieve rides by rider ID
- Filter rides by status
- Postman testing of the implemented ride retrieval and filtering features

### Current Focus

Expanding ride management capabilities and implementing the core ride lifecycle.

### Planned Features

#### Ride Lifecycle
- Filter rides by driver ID
- Driver acceptance and rejection
- Ride cancellation
- Ride start and completion
- Fare calculation
- Ride history

#### Backend Quality
- Unit and integration testing
- Testcontainers with PostgreSQL
- Database indexing and transaction management
- Swagger / OpenAPI documentation

#### Security & Performance
- Spring Security and JWT authentication
- Redis caching

#### Deployment & Architecture
- Docker containerization
- CI/CD pipeline
- Cloud deployment
- Centralized logging and monitoring
- API Gateway and service discovery
- Gradual transition to microservices architecture

## Running Locally

### Prerequisites

- Java 21 or a compatible configured JDK
- Maven
- PostgreSQL

### Steps

1. Clone the repository:

   ```bash
   git clone https://github.com/Preyansh13/rideshare-backend.git
   cd rideshare-backend
   ```

2. Configure your PostgreSQL database and application properties.

3. Start the Spring Boot application:

   ```bash
   mvn spring-boot:run
   ```

4. Test the APIs using Postman.

> Configure database credentials through your local environment or an untracked configuration file. Do not commit passwords or other secrets.

## Learning Objectives

This project is being developed to strengthen practical backend engineering skills, including:

- Java and object-oriented programming
- Spring Boot REST API development
- Layered architecture and separation of concerns
- Spring Data JPA and Hibernate
- PostgreSQL relationships and constraints
- DTOs, validation, and exception handling
- API testing and debugging
- Git-based incremental development
- Backend scalability and system design fundamentals

---

**Repository:** [Preyansh13/rideshare-backend](https://github.com/Preyansh13/rideshare-backend)