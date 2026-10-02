# RideShare Backend

A scalable ride-sharing backend built with Java, Spring Boot, Spring Data JPA, PostgreSQL,and REST APIs.

## Tech Stack

* Java 21
* Spring Boot
* Spring Data JPA / Hibernate
* PostgreSQL
* Maven
* REST APIs
* Jakarta Bean Validation

## Current Features

### User Management

* User CRUD APIs
* Full user updates using `PUT`
* Partial user updates using `PATCH`
* DTO-based request and response handling
* Layered architecture (Controller → Service → Repository)
* PostgreSQL persistence with Spring Data JPA
* Email uniqueness validation
* User search by name and email
* Pagination and sorting

### Validation & Error Handling

* Request validation using Jakarta Bean Validation
* Required-field validation for name, email, and phone
* Email format validation
* 10-digit phone number validation
* Custom validation messages
* Global exception handling
* Structured API error responses
* Multiple field-level validation errors
* Handling for `400`, `404`, and `409` error scenarios

## Project Status

🚧 Currently under development.

**Completed:** User management module with CRUD operations, DTOs, validation, exception handling, search, pagination, and sorting.

**Current Focus:** Database-level constraints and refinement of the user module.

## Planned Features

* Database-level email uniqueness constraints
* Ride management
* Driver and rider workflows
* Authentication & authorization with Spring Security/JWT
* Redis caching
* Unit and integration testing
* Docker containerization
* API Gateway
* Service discovery
* Microservices architecture
* Centralized logging
* Swagger / OpenAPI documentation
* Cloud deployment