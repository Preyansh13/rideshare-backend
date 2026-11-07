# RideShare Backend

A ride-sharing backend built with **Java 21, Spring Boot, Spring Data JPA, PostgreSQL, & REST APIs**. The project follows a layered monolithic architecture and implements user, driver, and vehicle management, ride lifecycle operations, and a dedicated ride-offer workflow.

The goal is to build a practical backend engineering project demonstrating REST API design, relational database modeling, validation, transaction management, concurrency control, business-rule enforcement, automated testing, and Git-based development.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot 4.1.1
- **Persistence:** Spring Data JPA / Hibernate
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **API Design:** REST APIs
- **API Documentation:** Springdoc OpenAPI / Swagger UI
- **Validation:** Jakarta Bean Validation
- **Testing:** JUnit 5, Mockito, Spring MVC Test / MockMvc, Postman
- **Development Environment:** IntelliJ IDEA
- **Version Control:** Git and GitHub

## Features

### 1. User Management

- Create, retrieve, update, partially update, and delete users.
- DTO-based request and response handling.
- Layered architecture: Controller → Service → Repository.
- PostgreSQL persistence using Spring Data JPA.
- Application-level duplicate checks and database uniqueness constraints for email and phone.
- User roles: `RIDER` and `DRIVER`.
- Search users independently by name and email.
- Pagination and sorting through Spring Data `Pageable`.
- Validation for names, email addresses, and phone numbers.
- Structured exception handling for missing users and duplicate user details.
- Automatic `RIDER` role assignment during ordinary user creation.
- Automatic promotion to the `DRIVER` role during driver registration.

### 2. Driver Management

- Register a driver using an existing user, vehicle, and driving license number.
- Retrieve all drivers or a driver by ID.
- Update driver license and vehicle details.
- Update driver availability through a dedicated status endpoint.
- Driver availability states: `AVAILABLE`, `BUSY`, and `OFFLINE`.
- Prevent assigning one vehicle to multiple drivers.
- One-to-one relationships between a driver and their user, and between a driver and their vehicle.
- Database uniqueness and non-null constraints for driver relationships and license number.
- Driver lookup and availability validation during ride-offer operations.
- Driver availability updates during offer acceptance, ride cancellation, and ride completion.

### 3. Vehicle Management

- Create, retrieve, update, and delete vehicles.
- Vehicle entity with a unique, required registration number.
- Registration numbers are normalized by trimming whitespace and converting to uppercase.
- Vehicle type classification: `HATCHBACK`, `SEDAN`, `SUV`, and `MUV`.
- Driver-to-vehicle relationship.
- Prevent deletion of a vehicle while it is assigned to a driver.
- Database-level foreign key and uniqueness constraints.

### 4. Ride Management

- Create and retrieve ride requests.
- Retrieve rides by ID, rider ID, and driver ID.
- Filter rides by status.
- Ride lifecycle represented by a `RideStatus` enum.
- Automatic ride request timestamp using a JPA lifecycle callback.
- Driver assignment through the ride-offer workflow.
- Start accepted rides.
- Complete rides that are in progress.
- Cancel rides in eligible states.
- Restore driver availability when an accepted ride is cancelled or a ride is completed.
- Expire pending offers when a ride is cancelled.
- Validate ride status transitions and driver availability.
- Transaction-managed ride and driver state changes.
- DTO-based ride responses, including a nullable driver ID before assignment.
- Pessimistic write locking for ride operations involved in offer creation, acceptance, rejection, and cancellation.

### 5. Ride Offer Management

A dedicated offer workflow allows drivers to receive offers for requested rides and accept or reject them independently of the overall ride request.

- Create a ride offer for a specific driver.
- Accept a pending offer and assign the driver to the ride.
- Reject an offer without rejecting the entire ride request.
- Track offer states using `RideOfferStatus`.
- Prevent duplicate offers for the same ride-driver pair through application-level validation and a database uniqueness constraint.
- Validate ride status, driver availability, user role, and offer status before processing an offer.
- Use transaction management and pessimistic write locks on the ride and driver records in offer operations.
- Expire other pending offers when a driver accepts a ride.
- Expire pending offers when the associated ride is cancelled.
- Keep rejected offers distinct from expired offers.
- Record offer creation time automatically.

Offer statuses:

- `PENDING`
- `ACCEPTED`
- `REJECTED`
- `EXPIRED`

### 6. Validation and Error Handling

- Jakarta Bean Validation on supported request DTOs.
- Required-field, email-format, and phone-number validation.
- Custom validation messages.
- Global exception handling using `@RestControllerAdvice`.
- Structured API error responses containing HTTP status, message, request path, and timestamp.
- Field-level validation error responses.
- HTTP `400 Bad Request`, `404 Not Found`, and `409 Conflict` handling.
- Database constraint violation handling.
- Custom exceptions for missing users, drivers, vehicles, and rides.
- Business-rule validation for ride and offer lifecycle transitions.
- Rejection of invalid repeated state transitions, such as starting an already-started ride or accepting an offer that is no longer pending.

### 7. API Documentation

- Springdoc OpenAPI integration.
- OpenAPI metadata configured for the RideShare Backend API.
- Interactive Swagger UI for exploring and testing endpoints.
- API documentation generated from the application's REST API definitions.

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
| GET | `/api/users/search/name?name={name}` | Search users by name |
| GET | `/api/users/search/email?email={email}` | Search users by email |

### Driver APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/drivers` | Register a driver |
| GET | `/api/drivers` | Retrieve all drivers |
| GET | `/api/drivers/{driverId}` | Retrieve a driver by ID |
| PUT | `/api/drivers/{driverId}` | Update driver details |
| PATCH | `/api/drivers/{driverId}/status?status={status}` | Update driver availability |

### Vehicle APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/vehicles` | Register a vehicle |
| GET | `/api/vehicles` | Retrieve all vehicles |
| GET | `/api/vehicles/{id}` | Retrieve a vehicle by ID |
| PUT | `/api/vehicles/{id}` | Update vehicle details |
| DELETE | `/api/vehicles/{id}` | Delete a vehicle if it is not assigned |

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
| PATCH | `/api/rides/{rideId}/start` | Start an accepted ride |
| PATCH | `/api/rides/{rideId}/complete` | Complete a ride in progress |

### Ride Offer APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/rides/{rideId}/offers/{driverId}` | Create a ride offer |
| PATCH | `/api/rides/{rideId}/offers/{driverId}/accept` | Accept a pending offer |
| PATCH | `/api/rides/{rideId}/offers/{driverId}/reject` | Reject a pending offer |

The dedicated ride-offer endpoints handle driver acceptance and rejection. There are no active direct ride accept/reject routes in the current `RideController`.

### Utility APIs

| Method | Endpoint | Purpose |
|---|---|---|
| GET | `/api/health` | Return application health status |
| GET | `/api/hello` | Return a simple application availability message |

## Ride Lifecycle

The `RideStatus` enum contains:

- `REQUESTED`
- `ACCEPTED`
- `IN_PROGRESS`
- `COMPLETED`
- `CANCELLED`
- `REJECTED`

### 1. Ride Creation

A rider creates a ride request with pickup and drop-off locations. The service verifies that the rider exists. A new ride begins with `REQUESTED` status and receives its request timestamp automatically before persistence.

### 2. Ride Offer Creation

Endpoint:

`POST /api/rides/{rideId}/offers/{driverId}`

The application checks that:

1. The ride exists.
2. The ride is `REQUESTED` and has no assigned driver.
3. The driver exists and is `AVAILABLE`.
4. The associated user has the `DRIVER` role.
5. An offer for the same ride-driver pair does not already exist.

A successful request creates an offer with `PENDING` status and an automatically populated creation timestamp.

### 3. Accept a Ride Offer

Endpoint:

`PATCH /api/rides/{rideId}/offers/{driverId}/accept`

When a driver accepts a pending offer:

1. The ride is retrieved with a pessimistic write lock.
2. The ride must still be `REQUESTED` and unassigned.
3. The offer for that ride-driver pair must exist and be `PENDING`.
4. The driver is retrieved with a pessimistic write lock.
5. The driver must still be `AVAILABLE`.
6. The associated user must have the `DRIVER` role.
7. The offer changes to `ACCEPTED`.
8. The driver is assigned to the ride.
9. The ride changes to `ACCEPTED`.
10. The driver changes to `BUSY`.
11. Other pending offers for that ride are marked `EXPIRED`.

The related state changes are managed within a transaction. Locking the ride and driver rows helps serialize competing operations and prevent conflicting assignments.

### 4. Reject a Ride Offer

Endpoint:

`PATCH /api/rides/{rideId}/offers/{driverId}/reject`

Rejecting a pending offer:

- Changes the offer status to `REJECTED`.
- Leaves the ride in `REQUESTED` status if it has not been assigned.
- Does not reject the entire ride request.
- Does not independently change the driver's availability.
- Requires the ride to still be accepting driver responses and the associated user to have the `DRIVER` role.

The driver remains `AVAILABLE` unless another operation changes the driver's state.

### 5. Start a Ride

Endpoint:

`PATCH /api/rides/{rideId}/start`

Only a ride in `ACCEPTED` status can be started.

On success:

- The ride status changes to `IN_PROGRESS`.
- The assigned driver remains `BUSY`.

Invalid state transitions are rejected.

### 6. Complete a Ride

Endpoint:

`PATCH /api/rides/{rideId}/complete`

Only a ride in `IN_PROGRESS` status with an assigned driver can be completed.

On success:

- The ride status changes to `COMPLETED`.
- The driver remains associated with the ride for historical reference.
- The driver's status changes to `AVAILABLE`.

The ride and driver updates are managed within a transaction.

### 7. Cancel a Ride

Endpoint:

`PATCH /api/rides/{id}/cancel`

Cancellation is permitted for rides in `REQUESTED` or `ACCEPTED` status.

On successful cancellation:

- The ride status changes to `CANCELLED`.
- If a driver was assigned, that driver's status changes back to `AVAILABLE`.
- All pending offers associated with the ride change to `EXPIRED`.
- Previously rejected offers remain `REJECTED`.
- Attempts to cancel rides in other states are rejected.

The ride is retrieved with a pessimistic write lock. Cancellation and pending-offer expiration are coordinated within the transaction.

## Database Design and Integrity

The project uses PostgreSQL with Spring Data JPA and Hibernate.

### Core Relationships

- A `Driver` has a one-to-one relationship with a `User`.
- A `Driver` has a one-to-one relationship with a `Vehicle`.
- A `Ride` belongs to one rider (`User`).
- A ride can have zero or one assigned driver.
- A `RideOffer` belongs to one ride and one driver.
- A ride can have multiple offers over time, while a driver can receive offers for different rides.

### Driver Constraints

The `drivers` entity mapping defines:

- `user_id`: `NOT NULL` and `UNIQUE`.
- `vehicle_id`: `NOT NULL` and `UNIQUE`.
- `license_number`: `NOT NULL` and `UNIQUE`.
- `status`: `NOT NULL`.

### Vehicle and User Constraints

- Vehicle registration number is required and unique.
- User email and phone are mapped as unique columns.
- Foreign keys maintain the relationships between drivers, users, vehicles, rides, and ride offers.

### Ride Indexes

Indexes have been added and verified in the development PostgreSQL database for frequently queried ride columns:

- `rider_id` — supports rider-specific ride retrieval.
- `driver_id` — supports driver-specific ride retrieval.
- `status` — supports status-based filtering.

PostgreSQL uses B-tree indexes by default for ordinary indexes unless another index type is specified. Indexes can improve lookups but add storage and write overhead.

### Ride Offer Constraints

The `ride_offers` table mapping defines a named uniqueness constraint on `(ride_id, driver_id)` to prevent duplicate offers for the same ride-driver pair.

### Schema Management

Entity annotations describe the intended database mapping. The project currently uses Hibernate `ddl-auto=update` for development schema updates.

The driver constraints and ride indexes have also been checked against the development PostgreSQL database. Those manual database changes are not currently represented as versioned migration scripts. Flyway migrations are a planned improvement so schema changes can be reproduced consistently across environments.

## Automated Testing

The project uses **JUnit 5, Mockito, Spring MVC Test, and MockMvc** for service and controller testing.

The current source contains **147 test methods across 11 test classes**. The latest full test run reported by the project owner completed with all 147 tests passing.

### Test Classes

| Test Class | Test Methods | Focus |
|---|---:|---|
| `UserServiceTest` | 26 | User business logic and validation rules |
| `UserControllerTest` | 14 | User API request and response behavior |
| `VehicleServiceTest` | 13 | Vehicle business logic and constraints |
| `VehicleControllerTest` | 5 | Vehicle API behavior |
| `DriverServiceTest` | 15 | Driver registration, retrieval, updates, and assignment rules |
| `DriverControllerTest` | 10 | Driver API request and response behavior |
| `RideServiceTest` | 24 | Ride creation, retrieval, cancellation, start, and completion |
| `RideControllerTest` | 10 | Ride API behavior |
| `RideOfferServiceTest` | 26 | Offer creation, acceptance, rejection, and lifecycle rules |
| `RideOfferControllerTest` | 3 | Ride-offer API behavior |
| `RidesharingApplicationTests` | 1 | Spring application context smoke test |
| **Total** | **147** | |

### Important Tested Scenarios

- Successful and invalid user, vehicle, and driver operations.
- Missing-resource handling and duplicate-resource conflicts.
- Request validation and controller response behavior.
- Ride creation and retrieval.
- Ride cancellation, start, and completion.
- Rejection of invalid ride lifecycle transitions.
- Ride-offer creation and duplicate-offer prevention.
- Driver availability and role validation during offer operations.
- Successful offer acceptance and rejection.
- Rejection of already-processed offers and handling of missing offers.
- Driver availability updates when a ride is accepted, cancelled, or completed.

The service tests use Mockito to isolate business logic. Controller tests use `@WebMvcTest` and MockMvc to exercise HTTP-layer behavior. The current suite is primarily unit and MVC-slice testing; dedicated PostgreSQL integration and true multi-threaded concurrency tests remain planned.

### Running Tests

Run the complete test suite:

```bash
mvn clean test
```

Run a specific test class:

```bash
mvn -Dtest=RideOfferServiceTest test
```

Run an individual test method:

```bash
mvn -Dtest=RideOfferServiceTest#rejectOffer_shouldThrowException_WhenOfferIsAlreadyProcessed test
```

## Architecture

The project follows a **layered monolithic architecture**.

```text
Client / Postman / Swagger UI
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

- **Controller:** Handles HTTP requests, path variables, query parameters, request data, and response status codes.
- **Service:** Implements business rules, validates state transitions, coordinates operations, and manages transactions.
- **Repository:** Provides database access through Spring Data JPA.
- **Entity:** Represents persistent domain objects and relational mappings.
- **DTO:** Controls the data accepted by API requests and exposed through API responses.
- **Global exception handler:** Converts application exceptions into structured HTTP error responses.
- **OpenAPI configuration:** Provides API metadata for generated documentation.

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

User (Rider)
     |
     v
    Ride
     ├── Driver (optional assignment)
     └── RideOffer (multiple offers)
           ├── Driver
           └── Offer Status
```

- `User` represents a person using the platform.
- `Driver` contains driver-specific details, including license information and availability.
- `Vehicle` stores vehicle details and is associated with a driver.
- `Ride` represents a booking request associated with a rider and, optionally, an assigned driver.
- `RideOffer` represents an offer sent to a driver for a particular ride.

The project is currently a **monolithic application, not a microservices architecture**. Microservices, API Gateway, and service discovery are future architectural considerations rather than existing features.

## Project Status

**Status: Core user, driver, vehicle, ride, and ride-offer workflows implemented; 147 automated tests present, with the latest reported full test run passing.**

### Implemented

- User create, read, update, partial update, and delete operations.
- User search by name and email.
- User pagination and sorting.
- DTO-based request and response handling.
- Request validation and global exception handling.
- Application-level and database-level uniqueness checks for user email and phone.
- User, driver, vehicle, ride, and ride-offer domain models.
- PostgreSQL persistence and entity relationships.
- Driver registration, retrieval, update, and status update APIs.
- Vehicle create, retrieve, update, and delete APIs.
- Vehicle assignment protection and prevention of deleting an assigned vehicle.
- Ride creation and retrieval.
- Ride filtering by rider, driver, and status.
- Dedicated ride-offer creation, acceptance, and rejection.
- Duplicate ride-offer protection.
- Ride cancellation, start, and completion.
- Pending-offer expiration on ride cancellation and competing-offer expiration on acceptance.
- Driver assignment and availability updates throughout the ride lifecycle.
- Transaction-managed business operations.
- Pessimistic write locking for ride-offer operations and ride cancellation.
- Database constraints and ride indexes in the development database.
- Swagger / OpenAPI integration.
- Manual Postman verification of core workflows.
- JUnit 5, Mockito, and MockMvc test suites with 147 test methods across 11 classes.
- Git-based incremental development and GitHub commits.

### In Progress

- Improving automated coverage for edge cases across all domain workflows.
- Reviewing consistency of transaction and locking behavior across ride state transitions.
- Improving request validation consistency across all controllers.
- Preparing the application for repeatable integration testing against PostgreSQL.

### Planned Improvements

#### 1. Backend Quality and Testing

- Add PostgreSQL integration tests with Testcontainers.
- Automate end-to-end ride and offer lifecycle tests.
- Test simultaneous offer acceptance and duplicate creation using concurrent requests.
- Review transaction boundaries and driver availability consistency.
- Add Flyway database migrations.
- Add tests for database constraints and persistence behavior.
- Apply consistent Jakarta validation to all applicable controller request bodies.
- Review API response and error-response consistency.

#### 2. Security and Authentication

- Spring Security.
- JWT-based authentication.
- Role-based authorization for riders and drivers.
- Authorization checks so users can perform only permitted ride operations.
- Secure database credentials and environment-specific configuration.

#### 3. Additional Features

- Fare calculation.
- Ride history and richer filtering.
- Driver discovery and retrieval by availability.
- More complete driver availability management rules.
- Offer expiry based on a configurable timeout and scheduled processing.
- Additional ride and driver business rules as the workflow evolves.

#### 4. Performance and Deployment

- Redis caching where justified by measured access patterns.
- Docker containerization.
- CI/CD pipeline.
- Cloud deployment.
- Centralized logging and monitoring.
- API Gateway and service discovery only if the system evolves toward microservices.

## Running Locally

### Prerequisites

- Java 21 or a compatible configured JDK.
- Maven.
- PostgreSQL.

### 1. Clone the Repository

```bash
git clone https://github.com/Preyansh13/rideshare-backend.git
cd rideshare-backend
```

### 2. Configure PostgreSQL

Create a PostgreSQL database named `rideshare`, or update the datasource configuration to match your local database.

Configure the following properties using environment variables or an untracked local configuration file:

```properties
spring.datasource.url=jdbc:postgresql://localhost:5432/rideshare
spring.datasource.username=${DB_USERNAME}
spring.datasource.password=${DB_PASSWORD}
```

Do not commit database passwords, API keys, or other secrets. Keep local credentials outside version control.

### 3. Start the Application

```bash
mvn spring-boot:run
```

### 4. Explore API Documentation

Start the application and open Swagger UI:

`http://localhost:8080/swagger-ui/index.html`

The generated OpenAPI document is commonly available at:

`http://localhost:8080/v3/api-docs`

### 5. Check Application Health

```http
GET http://localhost:8080/api/health
```

### 6. Test the APIs

Use Postman or Swagger UI to send requests to the locally running application, typically at `http://localhost:8080`.

Example requests:

```http
GET    /api/users?page=0&size=10
GET    /api/users/search/name?name=Rahul
GET    /api/users/search/email?email=example%40mail.com
POST   /api/users
POST   /api/vehicles
POST   /api/drivers
GET    /api/drivers
PATCH  /api/drivers/1/status?status=AVAILABLE
POST   /api/rides
GET    /api/rides/4
GET    /api/rides/rider/8
GET    /api/rides/driver/1
GET    /api/rides/status/REQUESTED
POST   /api/rides/6/offers/3
PATCH  /api/rides/6/offers/3/accept
PATCH  /api/rides/6/offers/3/reject
PATCH  /api/rides/4/start
PATCH  /api/rides/4/complete
PATCH  /api/rides/8/cancel
```

These examples illustrate endpoint formats. Use IDs and states that exist in your own database. A ride offer must be pending to be accepted or rejected, and a ride must be accepted before it can be started.

## Learning Objectives

This project is designed to strengthen practical backend engineering skills, including:

- Java and object-oriented programming.
- Spring Boot REST API development.
- Layered architecture and separation of concerns.
- Spring Data JPA and Hibernate.
- PostgreSQL relationships, constraints, and indexing.
- DTOs, validation, and exception handling.
- Transaction management and JPA dirty checking.
- State machines and business-rule validation.
- Ride assignment and driver availability management.
- Ride-offer lifecycle modeling.
- Pessimistic database locking and concurrency fundamentals.
- Unit and MVC-slice testing with JUnit 5, Mockito, and MockMvc.
- API testing, debugging, and error handling.
- Git-based incremental development.
- Integration testing and backend system design fundamentals.