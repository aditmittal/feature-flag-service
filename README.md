# Feature Flag Service

A small multi-tenant feature flag service built with Java,
Spring Boot, Spring Data JPA, and H2.

## Current Status

The project currently contains the domain model and persistence layer.

Implemented:

- Project entity
- Feature flag entity
- Project-scoped feature flag repository
- Repository tests
- Cross-tenant isolation test

## Tech Stack

- Java 17
- Spring Boot 4.1.1
- Spring Data JPA
- H2
- Maven
- JUnit
- AssertJ

## Running the Application

```bash
./mvnw spring-boot:run