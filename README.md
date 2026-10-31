# RideShare Backend

A ride-sharing backend built with **Java 21, Spring Boot, Spring Data JPA, PostgreSQL, and REST APIs**. The project follows a layered monolithic architecture and implements user management, driver and vehicle management, ride lifecycle operations, and a dedicated ride-offer workflow.

The goal is to build a practical backend engineering project demonstrating REST API design, relational database modeling, validation, transaction management, concurrency control, business-rule enforcement, automated testing, and Git-based development.

## Tech Stack

- **Language:** Java 21
- **Framework:** Spring Boot
- **Persistence:** Spring Data JPA / Hibernate
- **Database:** PostgreSQL
- **Build Tool:** Maven
- **API Design:** REST APIs
- **API Documentation:** Swagger / OpenAPI
- **Validation:** Jakarta Bean Validation
- **Testing:** JUnit 5, Mockito, Postman
- **Development Environment:** IntelliJ IDEA
- **Version Control:** Git and GitHub

## Features

### 1. User Management

- User CRUD APIs.
- Full updates using PUT and partial updates using PATCH.
- DTO-based request and response handling.
- Layered architecture: Controller → Service → Repository.
- PostgreSQL persistence using Spring Data JPA.
- Application-level and database-level email uniqueness.
- User roles: `RIDER` and `DRIVER`.
- Search users by name and email.
- Pagination and sorting.
- Validation for names, emails, and phone numbers.
- Structured exception handling for missing users and duplicate email addresses.
- Automatic `RIDER` role assignment during ordinary user creation.

### 2. Driver Management

- Driver registration linked to a user and vehicle.
- Driver license information.
- Driver availability states: `AVAILABLE`, `BUSY`, and `OFFLINE`.
- Driver lookup and validation during ride assignment.
- Custom exceptions for missing drivers and invalid driver states.
- One-to-one relationships and database uniqueness constraints.
- Automatic assignment of the `DRIVER` role during driver creation.
- Driver availability updates during ride acceptance, cancellation, and completion.

### 3. Vehicle Management

- Vehicle entity with a unique registration number.
- Vehicle type classification: `HATCHBACK`, `SEDAN`, `SUV`, and `MUV`.
- Driver-to-vehicle relationship.
- Database-level foreign key and uniqueness constraints.

### 4. Ride Management

- Create and retrieve ride requests.
- Retrieve rides by ID, rider ID, and driver ID.
- Filter rides by status.
- Ride lifecycle represented by a `RideStatus` enum.
- Automatic ride request timestamp.
- Driver assignment through the ride-offer workflow.
- Start accepted rides.
- Complete rides that are in progress.
- Cancel eligible rides.
- Restore driver availability when an accepted ride is cancelled or a ride is completed.
- Expire pending offers when a ride is cancelled.
- Validate ride status transitions and driver availability.
- Transaction-managed ride and driver state changes.
- DTO-based ride responses.
- Ride locking to help prevent conflicting ride assignment operations.

### 5. Ride Offer Management

A dedicated offer workflow allows drivers to receive offers for requested rides and accept or reject them independently of the overall ride request.

- Create a ride offer for a specific driver.
- Accept a pending offer and assign the driver to the ride.
- Reject an offer without rejecting the entire ride request.
- Track offer states using `RideOfferStatus`.
- Prevent duplicate offers for the same ride-driver pair through application-level validation and a database uniqueness constraint.
- Validate ride status, driver availability, and offer status before accepting an offer.
- Use transaction management and ride locking to help protect against conflicting state changes.
- Expire other pending offers when a driver accepts a ride.
- Expire pending offers when the associated ride is cancelled.
- Keep rejected offers distinct from expired offers.

Offer statuses:

- `PENDING`
- `ACCEPTED`
- `REJECTED`
- `EXPIRED`

### 6. Validation and Error Handling

- Jakarta Bean Validation.
- Required-field, email-format, and phone-number validation.
- Custom validation messages.
- Global exception handling.
- Structured API error responses containing status, message, path, and timestamp.
- Multiple field-level validation errors.
- HTTP `400 Bad Request`, `404 Not Found`, and `409 Conflict` handling.
- Database constraint violation handling.
- Custom exceptions for missing rides, drivers, and users.
- Business-rule validation for ride and offer lifecycle transitions.
- Rejection of invalid repeated state transitions, such as starting an already-started ride or accepting an offer that is no longer pending.

### 7. API Documentation

- Swagger / OpenAPI integration.
- Interactive API documentation for exploring and testing endpoints.
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
| GET | `/api/users/search` | Search users by name or email |

### Driver APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/drivers` | Register a driver with user, vehicle, and license details |

### Ride APIs

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/rides` | Create a ride request |
| GET | `/api/rides` | Retrieve rides |
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

The dedicated ride-offer endpoints handle driver acceptance and rejection. The older direct ride acceptance and rejection routes are not listed as active endpoints.

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

### 2. Ride Offer Creation

Endpoint:

`POST /api/rides/{rideId}/offers/{driverId}`

The application checks that:

1. The ride exists.
2. The ride is `REQUESTED` and has no assigned driver.
3. The driver exists and is `AVAILABLE`.
4. The associated user has the `DRIVER` role.
5. An offer for the same ride-driver pair does not already exist.

A successful request creates an offer with `PENDING` status.

### 3. Accept a Ride Offer

Endpoint:

`PATCH /api/rides/{rideId}/offers/{driverId}/accept`

When a driver accepts a pending offer:

1. The ride is retrieved with a database lock.
2. The offer must be `PENDING`.
3. The ride must still be `REQUESTED` and unassigned.
4. The driver must still be `AVAILABLE`.
5. The associated user must have the `DRIVER` role.
6. The offer changes to `ACCEPTED`.
7. The driver is assigned to the ride.
8. The ride changes to `ACCEPTED`.
9. The driver changes to `BUSY`.
10. Other pending offers for that ride are marked `EXPIRED`.

The related state changes are managed within a transaction. Ride locking helps serialize competing operations involving the same ride.

### 4. Reject a Ride Offer

Endpoint:

`PATCH /api/rides/{rideId}/offers/{driverId}/reject`

Rejecting a pending offer:

- Changes the offer status to `REJECTED`.
- Leaves the ride in `REQUESTED` status if it has not been assigned.
- Does not reject the entire ride request.
- Does not independently change the driver's availability.

The driver remains `AVAILABLE` if no other active assignment requires a busy state.

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

Only a ride in `IN_PROGRESS` status can be completed.

On success:

- The ride status changes to `COMPLETED`.
- The driver remains associated with the ride for historical reference.
- The driver's status changes from `BUSY` to `AVAILABLE`.

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

The cancellation and pending-offer expiration operations are integrated into the cancellation workflow and managed transactionally.

## Database Design and Integrity

The project uses PostgreSQL with Spring Data JPA and Hibernate.

### Driver Constraints

Driver records use database constraints to protect required fields and prevent duplicate relationships:

- `user_id`: `NOT NULL` and `UNIQUE`.
- `vehicle_id`: `NOT NULL` and `UNIQUE`.
- `license_number`: `NOT NULL` and `UNIQUE`.
- `status`: `NOT NULL`.

### Ride Indexes

Indexes have been created for frequently queried ride columns:

- `rider_id` — supports rider-specific ride retrieval.
- `driver_id` — supports driver-specific ride retrieval.
- `status` — supports status-based filtering.

PostgreSQL uses B-tree indexes by default for ordinary indexes unless another index type is specified. Indexes can improve lookups but add storage and write overhead.

### Ride Offer Constraints

The `ride_offers` table includes a uniqueness constraint on `(ride_id, driver_id)` to prevent duplicate offers for the same ride-driver pair.

### Schema Management

Entity annotations describe the intended database mapping, but existing database schemas may not receive every change through Hibernate's `ddl-auto=update`.

The project has verified the relevant driver constraints and ride indexes against PostgreSQL. Versioned database migrations with Flyway remain a planned improvement.

## Automated Testing

The project uses **JUnit 5 and Mockito** for service and controller unit testing.

Current test classes include:

- `RideControllerTest`
- `RideServiceTest`
- `RideOfferServiceTest`

### Ride Controller Tests

- Controller-level behavior and response handling.

### Ride Service Tests

- Ride cancellation.
- Ride start and completion.
- Valid and invalid lifecycle transitions.
- Missing ride handling.
- Driver availability behavior.

### Ride Offer Service Tests

Completed test scenarios include:

- Creating a valid ride offer.
- Preventing duplicate offers.
- Rejecting offer creation when the driver is unavailable.
- Successfully rejecting a pending offer.
- Preventing rejection of an already-processed offer.
- Handling a nonexistent offer.
- Handling a nonexistent ride.

Additional `acceptOffer()` unit tests and broader integration/concurrency tests are still in progress.

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
mvn -Dtest=RideOfferServiceTest#rejectOffer_ShouldThrowException_WhenOfferIsAlreadyProcessed test
```

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
- **Service:** Implements business rules, validates state transitions, coordinates operations, and manages transactions.
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
 ├── Driver (optional)
 └── RideOffer (multiple offers over time)
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

**Status: Core ride lifecycle and ride-offer management implemented; automated testing and backend quality improvements are in progress.**

### Implemented

- User CRUD, search, pagination, and sorting.
- DTO-based request and response handling.
- Validation and global exception handling.
- Application-level and database-level email uniqueness.
- User, driver, vehicle, ride, and ride-offer domain models.
- PostgreSQL persistence and entity relationships.
- Driver registration and user-role assignment.
- Ride creation and retrieval.
- Ride filtering by rider, driver, and status.
- Dedicated ride-offer creation, acceptance, and rejection.
- Duplicate ride-offer protection.
- Ride cancellation, start, and completion.
- Pending-offer expiration on ride cancellation.
- Driver assignment and availability updates throughout the ride lifecycle.
- Transaction-managed business operations and ride locking in the offer workflow.
- Database constraints and indexes.
- Swagger / OpenAPI integration.
- Manual Postman verification of core ride and offer workflows.
- JUnit 5 and Mockito test setup with controller and service tests.
- Git-based incremental development and GitHub commits.

### In Progress

- Completing automated `RideOfferService` unit tests.
- Adding automated tests for offer acceptance.
- Expanding test coverage for edge cases and business-rule enforcement.

### Planned Improvements

#### 1. Backend Quality and Testing

- Add integration tests with PostgreSQL and Testcontainers.
- Automate end-to-end ride and offer lifecycle tests.
- Test simultaneous offer acceptance and duplicate creation under concurrency.
- Review transaction boundaries and driver availability consistency.
- Add Flyway database migrations.
- Review and strengthen API response consistency.

#### 2. Security and Authentication

- Spring Security.
- JWT-based authentication.
- Role-based authorization for riders and drivers.
- Authorization checks so users can perform only permitted ride operations.

#### 3. Additional Features

- Fare calculation.
- Ride history and richer filtering.
- Driver retrieval, update, and availability management endpoints.
- More complete offer lifecycle and expiration handling.

#### 4. Performance and Deployment

- Redis caching where justified.
- Docker containerization.
- CI/CD pipeline.
- Cloud deployment.
- Centralized logging and monitoring.
- API Gateway and service discovery if the system evolves toward microservices.

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

Create the required database and configure your local database connection.

Use environment variables or an untracked configuration file for credentials. Never commit database passwords, API keys, or other secrets.

### 3. Start the Application

```bash
mvn spring-boot:run
```

### 4. Explore API Documentation

Start the application and open the Swagger UI URL configured in the project. The commonly used Springdoc UI path is:

`http://localhost:8080/swagger-ui/index.html`

### 5. Test the APIs

Use Postman or Swagger UI to send requests to the locally running application, typically at `http://localhost:8080`.

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
PATCH /api/rides/4/start
PATCH /api/rides/4/complete
POST  /api/rides/6/offers/3
PATCH /api/rides/6/offers/3/accept
PATCH /api/rides/6/offers/3/reject
PATCH /api/rides/8/cancel
```

These examples illustrate endpoint formats. Use IDs and states that exist in your own database. An offer must be pending to be accepted or rejected, and a ride must be accepted before it can be started.

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
- Database locking and concurrency fundamentals.
- Unit testing with JUnit 5 and Mockito.
- API testing, debugging, and error handling.
- Git-based incremental development.
- Automated testing and backend system design fundamentals.
