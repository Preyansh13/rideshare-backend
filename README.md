# RideShare Backend

A ride-sharing backend built with **Java 21, Spring Boot, Spring Data JPA, PostgreSQL, and REST APIs**. This project follows a layered monolithic architecture and implements user management, driver and vehicle management, ride assignment, and ride lifecycle operations.

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
- **Version Control:** Git and GitHub

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
- Request validation for names, emails, and phone numbers
- Structured exception handling for missing users and duplicate email addresses

### 2. Driver Management

- Driver registration linked to a user and vehicle
- Driver license information
- Driver availability states: `AVAILABLE`, `BUSY`, and `OFFLINE`
- Driver lookup and validation during ride acceptance
- Custom exceptions for missing drivers and invalid driver states
- One-to-one relationships and database uniqueness constraints
- Driver availability updates during ride acceptance, cancellation, and completion

### 3. Vehicle Management

- Vehicle entity with a unique registration number
- Vehicle type classification: `HATCHBACK`, `SEDAN`, `SUV`, and `MUV`
- Driver-to-vehicle relationship
- Database-level foreign key and uniqueness constraints

### 4. Ride Management

- Create and retrieve ride requests
- Retrieve rides by ID, rider ID, and driver ID
- Filter rides by status
- Ride lifecycle represented by a `RideStatus` enum
- Automatic ride request timestamp
- Driver acceptance and rejection of ride requests
- Driver assignment during ride acceptance
- Start accepted rides
- Complete rides that are in progress
- Cancel rides when their current status permits cancellation
- Restore driver availability when an accepted ride is cancelled or a ride is completed
- Validate ride status transitions and driver availability
- Transaction-managed ride and driver state changes
- DTO-based ride responses

### 5. Validation and Error Handling

- Jakarta Bean Validation
- Required-field, email-format, and phone-number validation
- Custom validation messages
- Global exception handling
- Structured API error responses containing status, message, path, and timestamp
- Multiple field-level validation errors
- HTTP `400 Bad Request`, `404 Not Found`, and `409 Conflict` handling
- Database constraint violation handling
- Custom exceptions for missing rides, drivers, and users
- Business-rule validation for ride acceptance, rejection, cancellation, starting, and completion

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

### Ride APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/rides` | Create a ride request |
| GET | `/api/rides` | Retrieve all rides |
| GET | `/api/rides/{id}` | Retrieve a ride by ID |
| GET | `/api/rides/rider/{riderId}` | Retrieve rides belonging to a rider |
| GET | `/api/rides/driver/{driverId}` | Retrieve rides assigned to a driver |
| GET | `/api/rides/status/{status}` | Filter rides by status |
| PATCH | `/api/rides/{id}/cancel` | Cancel an eligible ride |
| PATCH | `/api/rides/{rideId}/accept/{driverId}` | Accept a requested ride |
| PATCH | `/api/rides/{rideId}/reject/{driverId}` | Reject a requested ride |
| PATCH | `/api/rides/{rideId}/start` | Start an accepted ride |
| PATCH | `/api/rides/{rideId}/complete` | Complete a ride in progress |

Use IDs that exist in your database when testing. Example IDs in this README are illustrative and may not match your current database state.

## Ride Lifecycle

The `RideStatus` enum contains:

- `REQUESTED`
- `ACCEPTED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`
- `REJECTED`

### 1. Ride Creation

A rider creates a ride request with pickup and drop-off locations. A new ride begins with `REQUESTED` status and receives its request timestamp automatically.

### 2. Driver Acceptance

Endpoint:

`PATCH /api/rides/{rideId}/accept/{driverId}`

Business rules:

1. The ride must exist and have `REQUESTED` status.
2. The driver must exist and have `AVAILABLE` status.
3. The driver is assigned to the ride.
4. The ride status changes to `ACCEPTED`.
5. The driver's status changes to `BUSY`.

If the ride is in an invalid state or the driver is unavailable, the API rejects the operation with an appropriate error response.

### 3. Driver Rejection

Endpoint:

`PATCH /api/rides/{rideId}/reject/{driverId}`

A requested ride can be rejected by the specified driver. The ride status changes to `REJECTED`. Rejected rides cannot subsequently be accepted through the normal acceptance flow.

### 4. Start a Ride

Endpoint:

`PATCH /api/rides/{rideId}/start`

Only a ride in `ACCEPTED` status can be started.

On success, the ride status changes to `IN_PROGRESS`. The assigned driver remains `BUSY` while the journey is underway.

### 5. Complete a Ride

Endpoint:

`PATCH /api/rides/{rideId}/complete`

Only a ride in `IN_PROGRESS` status can be completed.

On success:

- The ride status changes to `COMPLETED`.
- The driver remains associated with the completed ride for historical reference.
- The driver's status changes from `BUSY` to `AVAILABLE`.

The ride and driver updates are managed within a transaction to help preserve database consistency.

### 6. Cancel a Ride

Endpoint:

`PATCH /api/rides/{id}/cancel`

Cancellation is permitted for rides in `REQUESTED` or `ACCEPTED` status.

- The ride status changes to `CANCELLED`.
- If a driver was assigned, that driver's status changes back to `AVAILABLE`.
- Attempts to cancel rides in other states are rejected.

## Verified Business Rules and Testing

The following scenarios have been verified through Postman during development:

| Scenario | Expected result | Verification |
|---|---|---|
| Create a valid user | `201 Created` | Passed |
| Create a user with duplicate email | `409 Conflict` | Passed |
| Submit invalid user data | `400 Bad Request` | Passed |
| Retrieve a nonexistent user | `404 Not Found` | Passed |
| Cancel an eligible ride | Successful cancellation | Passed |
| Reject a requested ride | Ride becomes `REJECTED` | Passed |
| Accept a requested ride with an available driver | Ride becomes `ACCEPTED`; driver becomes `BUSY` | Passed |
| Accept a cancelled ride | `409 Conflict` | Passed |
| Accept another ride with a busy driver | `409 Conflict`; new ride remains unassigned | Passed |
| Start an accepted ride | `200 OK`; ride becomes `IN_PROGRESS` | Passed |
| Start an already-started ride | `409 Conflict` | Passed |
| Start a nonexistent ride | `404 Not Found` | Passed |
| Complete an in-progress ride | `200 OK`; ride becomes `COMPLETED` | Passed |
| Complete an already-completed ride | `409 Conflict` | Passed |
| Complete a nonexistent ride | `404 Not Found` | Passed |
| Verify driver availability after completion | Driver becomes `AVAILABLE` | Passed |

Compilation has also passed for the latest service implementations.

These checks are manual Postman tests. Automated unit and integration test coverage remains future work.

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

### Layer Responsibilities

- **Controller:** Handles HTTP requests, path variables, request data, and response status codes.
- **Service:** Implements business rules, validates ride transitions, coordinates operations, and manages transactions.
- **Repository:** Provides database access through Spring Data JPA.
- **Entity:** Represents persistent domain objects.
- **DTO:** Controls the data exposed through API requests and responses.
- **Global exception handler:** Converts application exceptions into structured HTTP error responses.

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
- `Driver` contains driver-specific details, including license information and availability.
- `Vehicle` stores vehicle details and is associated with a driver.
- `Ride` represents a booking request associated with a rider and, optionally, an assigned driver.

The project is currently a **monolithic application, not a microservices architecture**. Microservices, API Gateway, and service discovery are future architectural considerations rather than existing features.

## Project Status

**Status: Under active development — core ride lifecycle implemented.**

### Implemented

- User CRUD, search, pagination, and sorting
- DTO-based request and response handling
- Validation and global exception handling
- Application-level and database-level email uniqueness
- User, driver, vehicle, and ride domain models
- PostgreSQL persistence and entity relationships
- Driver registration
- Ride creation and retrieval
- Ride filtering by rider, driver, and status
- Ride acceptance and rejection
- Ride cancellation with driver availability restoration
- Ride start and completion
- Driver assignment and availability updates throughout the ride lifecycle
- Custom exceptions and HTTP error responses
- Transaction-managed business operations
- Manual Postman verification of core ride workflows

### Planned Improvements

#### Backend Quality and Testing

- Unit tests for services and controllers
- Integration tests with PostgreSQL and Testcontainers
- Automated testing of ride lifecycle transitions
- Concurrency control for simultaneous driver acceptance
- Database indexing and query optimization
- Review and strengthen entity constraints and transaction boundaries
- Swagger / OpenAPI documentation

#### Security and Authentication

- Spring Security
- JWT-based authentication
- Role-based authorization for riders and drivers
- Authorization checks to ensure users can perform only permitted ride operations

#### Additional Features

- Fare calculation
- Ride history and richer filtering
- Driver retrieval, update, and availability management endpoints
- Improved API documentation and error consistency

#### Performance and Deployment

- Redis caching where justified
- Docker containerization
- CI/CD pipeline
- Cloud deployment
- Centralized logging and monitoring
- API Gateway and service discovery if the system evolves toward microservices

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

Create the required database and configure your local database connection.

Use environment variables or an untracked configuration file for credentials. Never commit database passwords, API keys, or other secrets.

### 3. Start the Application

```bash
mvn spring-boot:run
```

### 4. Test the APIs

Use Postman to send requests to the locally running application, typically at `http://localhost:8080`.

Example requests:

```http
GET   /api/users
GET   /api/users/search?name=Rahul
POST  /api/drivers
POST  /api/rides
GET   /api/rides/4
GET   /api/rides/rider/8
GET   /api/rides/driver/1
GET   /api/rides/status/REQUESTED
PATCH /api/rides/4/accept/1
PATCH /api/rides/4/start
PATCH /api/rides/4/complete
```

These examples illustrate the endpoint formats. Each operation must be tested against the actual state of the database; for example, a ride must be accepted before it can be started.

## Learning Objectives

This project is designed to strengthen practical backend engineering skills, including:

- Java and object-oriented programming
- Spring Boot REST API development
- Layered architecture and separation of concerns
- Spring Data JPA and Hibernate
- PostgreSQL relationships, constraints, and indexing
- DTOs, validation, and exception handling
- Transaction management and JPA dirty checking
- State machines and business-rule validation
- Ride assignment and driver availability management
- API testing, debugging, and error handling
- Git-based incremental development
- Automated testing, concurrency, and backend system design fundamentals
