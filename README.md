# RideShare Backend

A ride-sharing backend built with **Java 21, Spring Boot, Spring Data JPA, PostgreSQL, and REST APIs**. The project follows a layered monolithic architecture and focuses on building a strong foundation for ride management, driver assignment, and ride lifecycle operations.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot
- **Persistence:** Spring Data JPA / Hibernate
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **API Design:** REST APIs
- **Validation:** Jakarta Bean Validation
- **Testing:** Postman
- **Development Environment:** IntelliJ IDEA

## Current Features

### 1. User Management

- User CRUD APIs
- Full updates using PUT and partial updates using PATCH
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
- Driver-to-Vehicle relationship
- Database-level foreign key and uniqueness constraints

### 3. Ride Management

- Ride entity with rider and optional driver relationships
- Ride status management using an enum
- Automatic ride request timestamp
- Create ride API
- Retrieve a ride by ID
- Retrieve all rides
- Retrieve rides by rider ID
- Filter rides by driver ID
- Filter rides by status
- Cancel rides when their current status permits cancellation
- Transaction-managed ride creation and cancellation
- Custom exceptions for missing rides and invalid ride status transitions
- DTO-based ride responses

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

The following endpoints have been implemented.

### User APIs

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/users` | Retrieve users with pagination and sorting |
| GET | `/api/users/{id}` | Retrieve a user by ID |
| POST | `/api/users` | Create a user |
| PUT | `/api/users/{id}` | Fully update a user |
| PATCH | `/api/users/{id}` | Partially update a user |
| DELETE | `/api/users/{id}` | Delete a user |
| GET | `/api/users/search` | Search users by name or email |

### Ride APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/rides` | Create a ride request |
| GET | `/api/rides` | Retrieve all rides |
| GET | `/api/rides/{id}` | Retrieve a ride by ID |
| GET | `/api/rides/rider/{riderId}` | Retrieve rides belonging to a rider |
| GET | `/api/rides/driver/{driverId}` | Retrieve rides assigned to a driver |
| GET | `/api/rides/status/{status}` | Filter rides by status |
| PATCH | `/api/rides/{id}/cancel` | Cancel a ride if its status permits |

Example requests:

```http
GET   /api/rides
GET   /api/rides/1
GET   /api/rides/rider/8
GET   /api/rides/driver/1
GET   /api/rides/status/REQUESTED
PATCH /api/rides/1/cancel
```

Ride cancellation is allowed for rides in `REQUESTED` or `ACCEPTED` status. Attempts to cancel rides in other states are rejected with a conflict response.

## Architecture

The application currently follows a **layered monolithic architecture**.

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
 ├── Role: RIDER
 └── Role: DRIVER
       |
       v
     Driver
       |
       v
     Vehicle

Ride
 ├── Rider (User)
 └── Driver (optional)
```

- A `User` can have the role `RIDER` or `DRIVER`.
- A `Driver` stores driver-specific information, including a license number and availability status.
- A `Vehicle` stores vehicle information and is associated with a driver according to the domain relationships.
- A `Ride` represents a ride request associated with a rider and, optionally, an assigned driver.
- A `RideStatus` enum represents the ride lifecycle, including `REQUESTED`, `ACCEPTED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`, and `REJECTED`.

The project is currently a **monolithic application, not a microservices architecture**. API Gateway, service discovery, and microservices are potential future enhancements.

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
- Filter rides by status and driver ID
- Ride cancellation with status validation
- Custom exception handling for invalid ride status transitions
- Postman testing of implemented ride retrieval, filtering, and cancellation flows

### Current Focus

Expanding ride lifecycle operations, particularly driver assignment and ride status transitions.

### Planned Features

#### Ride Lifecycle

- Driver acceptance and rejection of ride requests
- Ride start and completion
- Driver availability updates during the ride lifecycle
- Fare calculation
- Ride history and additional filtering

#### Backend Quality

- Unit and integration testing
- Testcontainers with PostgreSQL
- Database indexing and query optimization
- Transaction management improvements
- Swagger / OpenAPI documentation

#### Security & Performance

- Spring Security and JWT authentication
- Redis caching
- Role-based authorization

#### Deployment & Architecture

- Docker containerization
- CI/CD pipeline
- Cloud deployment
- Centralized logging and monitoring
- API Gateway and service discovery
- Gradual transition to microservices, if justified by system requirements

## Running Locally

### Prerequisites

- Java 21 or a compatible configured JDK
- Maven
- PostgreSQL

### Steps

**1. Clone the repository**

```bash
git clone https://github.com/Preyansh13/rideshare-backend.git
cd rideshare-backend
```

**2. Configure PostgreSQL**

Create the required database and configure the database connection in your local application configuration.

Use environment variables or an untracked configuration file for credentials. Never commit database passwords, API keys, or other secrets.

**3. Start the application**

```bash
mvn spring-boot:run
```

**4. Test the APIs**

Use Postman to send requests to the locally running application, typically at `http://localhost:8080`.

## Learning Objectives

This project is being developed to strengthen practical backend engineering skills, including:

- Java and object-oriented programming
- Spring Boot REST API development
- Layered architecture and separation of concerns
- Spring Data JPA and Hibernate
- PostgreSQL relationships and constraints
- DTOs, validation, and exception handling
- Transaction management and business-rule validation
- API testing and debugging
- Git-based incremental development
- Backend scalability and system design fundamentals
