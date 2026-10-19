# RideShare Backend

A ride-sharing backend built with **Java 21, Spring Boot, Spring Data JPA, PostgreSQL, and REST APIs**. This project follows a layered monolithic architecture and focuses on user management, driver and vehicle management, ride assignment, and ride lifecycle operations.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot
- **Persistence:** Spring Data JPA / Hibernate
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **API Design:** REST APIs
- **Validation:** Jakarta Bean Validation
- **API Testing:** Postman
- **Development Environment:** IntelliJ IDEA

## Features

### 1. User Management

- User CRUD APIs
- Full updates using PUT and partial updates using PATCH
- DTO-based request and response handling
- Layered architecture: Controller → Service → Repository
- PostgreSQL persistence using Spring Data JPA
- Application-level and database-level email uniqueness
- User roles: `RIDER` and `DRIVER`
- Search users by name and email
- Pagination and sorting
- Structured exception handling for missing users and duplicate email addresses

### 2. Driver Management

- Driver entity linked to a user
- Driver registration with user, vehicle, and license information
- Driver availability states: `AVAILABLE`, `BUSY`, and `OFFLINE`
- Driver lookup and validation during ride acceptance
- Custom exceptions for missing drivers and invalid driver states
- PostgreSQL relationships and uniqueness constraints

### 3. Vehicle Management

- Vehicle entity with a unique registration number
- Vehicle type classification: `HATCHBACK`, `SEDAN`, `SUV`, and `MUV`
- Driver-to-vehicle relationship
- Database-level foreign key and uniqueness constraints

### 4. Ride Management

- Ride entity linked to a rider and optionally to an assigned driver
- Ride lifecycle represented by a `RideStatus` enum
- Automatic ride request timestamp
- Create and retrieve ride requests
- Retrieve rides by ID, rider ID, and driver ID
- Filter rides by status
- Cancel rides when their current status permits cancellation
- Accept requested rides for available drivers
- Assign drivers to accepted rides
- Update ride and driver statuses during acceptance
- Transaction-managed ride creation, cancellation, and acceptance
- DTO-based ride responses

### 5. Validation and Error Handling

- Jakarta Bean Validation for request data
- Required-field validation for names, emails, and phone numbers
- Email format validation
- 10-digit phone number validation
- Custom validation messages
- Global exception handling
- Structured API error responses
- Multiple field-level validation errors
- HTTP `400 Bad Request`, `404 Not Found`, and `409 Conflict` handling
- Database constraint violation handling
- Business-rule validation for ride acceptance and cancellation

## API Documentation

Base URL for local development:

`http://localhost:8080`

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

### Driver APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/drivers` | Register a driver with user, vehicle, and license details |

The driver registration endpoint uses the existing driver, user, and vehicle domain models. Additional driver retrieval, update, and availability endpoints can be documented once implemented and verified.

### Ride APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/rides` | Create a ride request |
| GET | `/api/rides` | Retrieve all rides |
| GET | `/api/rides/{id}` | Retrieve a ride by ID |
| GET | `/api/rides/rider/{riderId}` | Retrieve rides belonging to a rider |
| GET | `/api/rides/driver/{driverId}` | Retrieve rides assigned to a driver |
| GET | `/api/rides/status/{status}` | Filter rides by status |
| PATCH | `/api/rides/{id}/cancel` | Cancel a ride when its status permits |
| PATCH | `/api/rides/{rideId}/accept/{driverId}` | Accept a requested ride as a driver |

### Example Requests

```http
GET   /api/users
GET   /api/users/search?name=Rahul
POST  /api/drivers
POST  /api/rides
GET   /api/rides/1
GET   /api/rides/rider/8
GET   /api/rides/driver/1
GET   /api/rides/status/REQUESTED
PATCH /api/rides/1/cancel
PATCH /api/rides/2/accept/1
```

Use IDs that exist in your database when testing. The example IDs above are illustrative.

## Ride Lifecycle

The `RideStatus` enum currently includes:

- `REQUESTED`
- `ACCEPTED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`
- `REJECTED`

### Driver Acceptance

The endpoint `PATCH /api/rides/{rideId}/accept/{driverId}` validates the ride and driver before updating their states.

**Business rules:**

1. The ride must exist.
2. The ride must have `REQUESTED` status.
3. The driver must exist.
4. The driver must have `AVAILABLE` status.
5. On successful acceptance, the driver is assigned to the ride, the ride becomes `ACCEPTED`, and the driver's status becomes `BUSY`.

**Verified scenarios:**

- Successful acceptance returns `200 OK`.
- The ride changes to `ACCEPTED`.
- The driver is assigned to the ride and becomes `BUSY`.
- Attempting to accept a cancelled ride returns `409 Conflict`.
- Attempting to accept another ride with a busy driver returns `409 Conflict`.
- After a busy-driver rejection, the new ride remains `REQUESTED` and unassigned.

### Ride Cancellation

Ride cancellation is permitted for rides in `REQUESTED` or `ACCEPTED` status. Attempts to cancel rides in other states are rejected with a conflict response.

**Known improvement:** Cancelling an accepted ride should also return the assigned driver's status to `AVAILABLE`. This consistency rule remains a planned improvement.

## Architecture

The project follows a **layered monolithic architecture**.

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

- `User` represents a person using the platform.
- `Driver` stores driver-specific information, including license number and availability status.
- `Vehicle` stores vehicle information and is associated with a driver through the domain relationship.
- `Ride` represents a request associated with a rider and, optionally, an assigned driver.

The project is currently a **monolithic application, not a microservices architecture**. API Gateway, service discovery, and a potential transition to microservices are future architectural considerations, not current features.

## Project Status

**Status: Under active development**

### Implemented

- User CRUD APIs
- DTO-based API design
- Request validation and global exception handling
- Search, pagination, and sorting
- Application-level and database-level email uniqueness
- User roles and driver availability states
- Driver and vehicle domain entities
- PostgreSQL relationships and constraints
- Driver registration API
- Ride creation and retrieval APIs
- Ride filtering by rider, driver, and status
- Ride cancellation with status validation
- Driver acceptance of requested rides
- Driver assignment and availability updates during acceptance
- Custom exceptions for invalid ride and driver states
- Postman verification of ride acceptance and busy-driver rejection

### Current Focus

Expanding ride lifecycle operations and strengthening consistency across ride and driver status transitions.

### Planned Features

#### Ride Lifecycle

- Driver rejection of ride requests
- Start and complete rides
- Synchronize driver availability throughout the ride lifecycle
- Release a driver when an accepted ride is cancelled
- Fare calculation
- Ride history and additional filtering

#### Backend Quality

- Unit and integration testing
- Testcontainers with PostgreSQL
- Database indexing and query optimization
- Transaction management improvements
- Concurrency control for simultaneous ride acceptance
- Swagger / OpenAPI documentation

#### Security and Performance

- Spring Security and JWT authentication
- Role-based authorization
- Redis caching

#### Deployment and Architecture

- Docker containerization
- CI/CD pipeline
- Cloud deployment
- Centralized logging and monitoring
- API Gateway and service discovery
- Gradual transition to microservices if justified by system requirements

## Running Locally

### Prerequisites

- Java 21 or a compatible configured JDK
- Maven
- PostgreSQL

### 1. Clone the Repository

```bash
git clone https://github.com/Preyansh13/rideshare-backend.git
cd rideshare-backend
```

### 2. Configure PostgreSQL

Create the required database and configure the database connection in your local application configuration.

Use environment variables or an untracked configuration file for credentials. Never commit database passwords, API keys, or other secrets.

### 3. Start the Application

```bash
mvn spring-boot:run
```

### 4. Test the APIs

Use Postman to send requests to the locally running application, typically at `http://localhost:8080`.

## Learning Objectives

This project is designed to strengthen practical backend engineering skills, including:

- Java and object-oriented programming
- Spring Boot REST API development
- Layered architecture and separation of concerns
- Spring Data JPA and Hibernate
- PostgreSQL relationships and constraints
- DTOs, validation, and exception handling
- Transaction management and business-rule validation
- Ride lifecycle and state-transition design
- API testing and debugging
- Git-based incremental development
- Concurrency handling and backend system design fundamentals
