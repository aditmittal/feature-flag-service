# Feature Flag Service

A small multi-tenant feature flag service built with Java, Spring Boot,
Spring Data JPA, H2, Maven, JUnit, and AssertJ.

The service allows applications to create, manage, and evaluate feature
flags within a project/tenant boundary.

The main goal of this project is to demonstrate how a production-style
feature flag service can be designed with clean separation between
controllers, services, repositories, entities, validation, persistence,
and automated tests.

---

# 1. Project Overview

Feature flags allow application behavior to be changed without deploying
new application code.

For example, suppose an application has a new checkout implementation:

    checkout-v2

Instead of deploying different versions of the application, the
application can check whether the feature flag is enabled for a particular
project and user.

Example:

    GET /projects/payments/eval?flag=checkout-v2&user=alice

The service evaluates the flag and returns:

    true

or:

    false

The service supports three flag states:

    ON
    OFF
    DEFAULT

The behavior is:

| Flag State | Evaluation Result |
|------------|-------------------|
| ON         | true              |
| OFF        | false             |
| DEFAULT    | Deterministic result based on project-independent flag/user hash |

The important property of `DEFAULT` is that the same user and flag always
produce the same result.

---

# 2. Current Features

The application currently supports:

- Project persistence
- Feature flag persistence
- Project-scoped feature flags
- Multi-tenant isolation
- Create feature flag
- Get all feature flags
- Get a specific feature flag
- Update a feature flag
- Delete a feature flag
- Feature flag evaluation API
- ON/OFF/DEFAULT evaluation
- Stable deterministic DEFAULT evaluation
- Validation of API requests
- Resource-not-found handling
- Database-level uniqueness constraint
- H2 in-memory database
- Automatic test data for local development
- Separate test database configuration
- Repository tests
- Evaluation tests
- Automated Spring Boot tests
- Cross-project isolation tests

---

# 3. Tech Stack

| Technology | Purpose |
|------------|---------|
| Java 17 | Programming language |
| Spring Boot 4.1.1 | Application framework |
| Spring Web | REST APIs |
| Spring Data JPA | Persistence/repository abstraction |
| Hibernate | JPA implementation |
| H2 | In-memory relational database |
| Maven | Build and dependency management |
| JUnit 5 | Testing |
| AssertJ | Test assertions |

---

# 4. Architecture

The project follows a layered architecture.

```text
                    HTTP Request
                         |
                         v
              +----------------------+
              |     Controller       |
              |----------------------|
              | FeatureFlagController|
              | EvaluationController |
              +----------+-----------+
                         |
                         v
              +----------------------+
              |       Service        |
              |----------------------|
              |  FeatureFlagService  |
              +----------+-----------+
                         |
                         v
              +----------------------+
              |     Repository       |
              |----------------------|
              | ProjectRepository    |
              | FeatureFlagRepository|
              +----------+-----------+
                         |
                         v
              +----------------------+
              |        JPA           |
              |     Hibernate        |
              +----------+-----------+
                         |
                         v
              +----------------------+
              |         H2           |
              |      Database        |
              +----------------------+
```

## Responsibility of each layer

### Controller

Responsible for:

- Receiving HTTP requests
- Reading path variables
- Reading query parameters
- Reading request bodies
- Triggering validation
- Returning HTTP responses

Controllers should not contain business logic.

---

### Service

Responsible for:

- Business logic
- Project existence checks
- Feature flag creation
- Feature flag updates
- Feature flag deletion
- Feature flag evaluation
- ON/OFF/DEFAULT behavior
- Deterministic DEFAULT evaluation

The service layer acts as the main business boundary.

---

### Repository

Responsible for:

- Database access
- Querying feature flags
- Checking whether a feature flag exists
- Finding flags within a specific project

Spring Data JPA generates most of the implementation automatically.

---

### Entity

Represents persistent database data.

Main entities:

```text
Project
FeatureFlag
```

---

# 5. Domain Model

## Project

A project represents a tenant/application boundary.

```java
@Entity
@Table(name = "projects")
public class Project {

    @Id
    private String id;

    private String name;
}
```

Example:

```text
id      = payments
name    = Payments
```

---

## FeatureFlag

A feature flag belongs to exactly one project.

```text
FeatureFlag
    |
    +-- id
    +-- name
    +-- state
    +-- project
```

The relationship is:

```java
@ManyToOne(fetch = FetchType.LAZY, optional = false)
@JoinColumn(name = "project_id", nullable = false)
private Project project;
```

Therefore:

```text
Project
   |
   +---- FeatureFlag
   |
   +---- FeatureFlag
   |
   +---- FeatureFlag
```

A project can contain multiple feature flags.

---

# 6. Flag States

The project defines:

```java
public enum FlagState {
    ON,
    OFF,
    DEFAULT
}
```

## ON

Always evaluates to:

```text
true
```

Example:

```text
checkout-v2 = ON

alice -> true
bob   -> true
charlie -> true
```

---

## OFF

Always evaluates to:

```text
false
```

Example:

```text
checkout-v2 = OFF

alice -> false
bob   -> false
charlie -> false
```

---

## DEFAULT

DEFAULT does not simply return a random result.

Instead, the result is deterministic.

Conceptually:

```text
flag + user
     |
     v
   hash
     |
     v
 deterministic boolean
```

For example:

```java
Math.abs((flagName + ":" + user).hashCode()) % 2 == 0
```

This means the same input always produces the same result during the
same application/runtime assumptions.

Example:

```text
flag = checkout-v2
user = alice

first evaluation  -> true
second evaluation -> true
third evaluation  -> true
```

This is important because a user should not see the feature randomly
switching between enabled and disabled states.

---

# 7. Multi-Tenant Isolation

One of the most important design requirements is tenant isolation.

A feature flag is identified within the scope of a project.

The database therefore contains a unique constraint:

```java
@UniqueConstraint(
    name = "uk_feature_flag_project_name",
    columnNames = {"project_id", "name"}
)
```

This means:

```text
project        flag
--------------------------
payments       checkout-v2
payments       search-v2
mobile         checkout-v2
```

is valid.

But:

```text
payments       checkout-v2
payments       checkout-v2
```

is not valid.

The same flag name can therefore exist in multiple projects while
remaining unique inside each project.

---

# 8. Repository Queries

Feature flag queries are project-scoped.

For example:

```java
findByProjectIdAndName(projectId, name)
```

and:

```java
existsByProjectIdAndName(projectId, name)
```

This is important for tenant isolation.

We do NOT want to perform a global lookup such as:

```text
findByName(name)
```

because the same flag name can exist in multiple projects.

The project ID must be part of the lookup.

---

# 9. REST APIs

Base URL:

```text
http://localhost:8080
```

---

## 9.1 Create Feature Flag

### Request

```http
POST /projects/{projectId}/flags
```

Example:

```http
POST http://localhost:8080/projects/payments/flags
```

Request body:

```json
{
  "name": "checkout-v2",
  "state": "ON"
}
```

### Response

HTTP:

```text
201 Created
```

Response:

```json
{
  "name": "checkout-v2",
  "state": "ON"
}
```

---

# 9.2 Get All Feature Flags

### Request

```http
GET /projects/{projectId}/flags
```

Example:

```http
GET http://localhost:8080/projects/payments/flags
```

Example response:

```json
[
  {
    "name": "checkout-v2",
    "state": "ON"
  },
  {
    "name": "search-v2",
    "state": "OFF"
  }
]
```

---

# 9.3 Get a Specific Feature Flag

### Request

```http
GET /projects/{projectId}/flags/{name}
```

Example:

```http
GET http://localhost:8080/projects/payments/flags/checkout-v2
```

Example response:

```json
{
  "name": "checkout-v2",
  "state": "ON"
}
```

---

# 9.4 Update Feature Flag

### Request

```http
PUT /projects/{projectId}/flags/{name}
```

Example:

```http
PUT http://localhost:8080/projects/payments/flags/checkout-v2
```

Request body:

```json
{
  "state": "OFF"
}
```

Example response:

```json
{
  "name": "checkout-v2",
  "state": "OFF"
}
```

---

# 9.5 Delete Feature Flag

### Request

```http
DELETE /projects/{projectId}/flags/{name}
```

Example:

```http
DELETE http://localhost:8080/projects/payments/flags/checkout-v2
```

Expected response:

```text
204 No Content
```

---

# 9.6 Evaluate Feature Flag

This is the main business API.

### Request

```http
GET /projects/{projectId}/eval?flag={flagName}&user={user}
```

Example:

```http
GET http://localhost:8080/projects/payments/eval?flag=checkout-v2&user=alice
```

The API evaluates the flag for the specified user.

Possible results:

```json
{
  "enabled": true
}
```

or:

```json
{
  "enabled": false
}
```

---

# 10. Evaluation Rules

The evaluation logic follows:

```text
                 Feature Flag
                      |
          +-----------+-----------+
          |           |           |
          v           v           v
         ON          OFF       DEFAULT
          |           |           |
          v           v           v
        true        false       hash
                                  |
                                  v
                           deterministic
                            true/false
```

More explicitly:

```text
ON      -> true
OFF     -> false
DEFAULT -> deterministic hash(flag + user)
```

---

# 11. Why Deterministic Evaluation?

A naive implementation could use:

```java
new Random().nextBoolean()
```

That would be incorrect for a feature flag system because the same user
could receive different results on different requests.

For example:

```text
Request 1 -> true
Request 2 -> false
Request 3 -> true
Request 4 -> false
```

This creates an inconsistent user experience.

Instead we use deterministic hashing.

```text
flag + user
    |
    v
stable hash
    |
    v
same result
```

Therefore:

```text
alice + checkout-v2 -> always same result
```

This is also the foundation for percentage-based rollouts and
consistent bucketing in more advanced feature flag systems.

---

# 12. Database

The application currently uses H2:

```properties
spring.datasource.url=jdbc:h2:mem:featureflagdb
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
```

Hibernate creates the database schema automatically:

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

Because this is an in-memory database, the database is recreated when
the application restarts.

---

# 13. H2 Console

The H2 console is enabled:

```properties
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console
```

Open:

```text
http://localhost:8080/h2-console
```

Use:

```text
JDBC URL:
jdbc:h2:mem:featureflagdb

User Name:
sa

Password:
```

The password is empty.

---

# 14. Seed Data

The project contains:

```text
src/main/resources/data.sql
```

This file inserts required development data into H2 when the application
starts.

This is useful because H2 is an in-memory database.

Without seed data:

```text
Application restart
       |
       v
New empty database
```

With `data.sql`:

```text
Application restart
       |
       v
New database
       |
       v
Seed data inserted
       |
       v
APIs immediately testable
```

---

# 15. Example Seed Data

The application can contain project/feature flag data such as:

```text
Project:
payments

Feature Flags:
checkout-v2
checkout-v3
```

This allows the evaluation API to be tested immediately after startup.

---

# 16. Testing

The project uses:

```text
JUnit 5
AssertJ
Spring Boot Test
Spring Data JPA Test
```

Run all tests with:

```bash
./mvnw test
```

On Windows:

```powershell
.\mvnw.cmd test
```

A successful run should show:

```text
Tests run: 14
Failures: 0
Errors: 0
Skipped: 0
```

---

# 17. Test Categories

The tests cover multiple layers of the application.

## Repository Tests

Repository tests verify:

- Finding a feature flag by project and name
- Checking whether a flag exists
- Project-scoped lookups
- Cross-project isolation
- Database uniqueness constraints

---

## Evaluation Tests

Evaluation tests verify:

### ON

```text
ON -> true
```

### OFF

```text
OFF -> false
```

### DEFAULT

The same flag/user combination returns the same result.

Example:

```text
First evaluation:
alice + checkout-v2 -> true

Second evaluation:
alice + checkout-v2 -> true
```

---

## Missing Flag

The evaluation API should fail when the requested flag does not exist.

This prevents silently evaluating an unknown feature.

---

## Cross-Project Isolation

Suppose:

```text
payments:
    checkout-v2 = ON

mobile:
    checkout-v2 = OFF
```

A request for:

```text
/projects/mobile/eval?flag=checkout-v2&user=alice
```

must evaluate the `mobile` flag.

It must never accidentally return the `payments` flag.

This is a critical multi-tenant requirement.

---

# 18. Test Database Configuration

The project contains a test-specific properties file:

```text
src/test/resources/application.properties
```

This allows tests to use their own database configuration instead of
depending on local runtime data.

This separation is important because:

```text
Development DB
        !=
Test DB
```

Tests should be repeatable and isolated.

---

# 19. Validation

The request DTOs use Jakarta validation.

For example:

```java
@Valid
@RequestBody CreateFeatureFlagRequest request
```

This allows invalid requests to be rejected before they reach the
business logic.

The architecture is:

```text
HTTP Request
     |
     v
Validation
     |
     v
Controller
     |
     v
Service
     |
     v
Repository
```

---

# 20. Exception Handling

The service uses:

```java
ResourceNotFoundException
```

when a requested resource does not exist.

Examples:

```text
Project does not exist
Feature flag does not exist
```

This keeps resource lookup failures inside the business/service layer
instead of scattering database checks throughout controllers.

---

# 21. Project Structure

The current project follows this structure:

```text
feature-flag-service/
│
├── src/
│   │
│   ├── main/
│   │   │
│   │   ├── java/
│   │   │   └── com/example/featureflag/
│   │   │       │
│   │   │       ├── controller/
│   │   │       │   ├── FeatureFlagController.java
│   │   │       │   └── FeatureFlagEvaluationController.java
│   │   │       │
│   │   │       ├── dto/
│   │   │       │   ├── CreateFeatureFlagRequest.java
│   │   │       │   ├── UpdateFeatureFlagRequest.java
│   │   │       │   └── FeatureFlagResponse.java
│   │   │       │
│   │   │       ├── entity/
│   │   │       │   ├── Project.java
│   │   │       │   ├── FeatureFlag.java
│   │   │       │   └── FlagState.java
│   │   │       │
│   │   │       ├── exception/
│   │   │       │   └── ResourceNotFoundException.java
│   │   │       │
│   │   │       ├── repository/
│   │   │       │   ├── ProjectRepository.java
│   │   │       │   └── FeatureFlagRepository.java
│   │   │       │
│   │   │       ├── service/
│   │   │       │   └── FeatureFlagService.java
│   │   │       │
│   │   │       └── FeatureFlagServiceApplication.java
│   │   │
│   │   └── resources/
│   │       ├── application.properties
│   │       └── data.sql
│   │
│   └── test/
│       │
│       ├── java/
│       │   └── com/example/featureflag/
│       │       ├── FeatureFlagRepositoryTest.java
│       │       ├── FeatureFlagEvaluationTest.java
│       │       └── FeatureFlagServiceApplicationTests.java
│       │
│       └── resources/
│           └── application.properties
│
├── pom.xml
├── mvnw
├── mvnw.cmd
└── README.md
```

---

# 22. Running the Application

## Clone the repository

```bash
git clone <repository-url>
```

Navigate into the project:

```bash
cd feature-flag-service
```

---

## Run the application

Linux/macOS:

```bash
./mvnw spring-boot:run
```

Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

The application starts on:

```text
http://localhost:8080
```

Expected log:

```text
Tomcat started on port 8080
```

---

# 23. Running Tests

Windows:

```powershell
.\mvnw.cmd clean test
```

Linux/macOS:

```bash
./mvnw clean test
```

Expected result:

```text
BUILD SUCCESS
```

---

# 24. Complete API Flow

A typical flow looks like:

```text
1. Create Project
        |
        v
2. Create Feature Flag
        |
        v
3. Read Feature Flag
        |
        v
4. Update Feature Flag
        |
        v
5. Evaluate Feature Flag
        |
        v
6. Delete Feature Flag
```

For example:

```text
Project:
payments

        |
        v

Feature Flag:
checkout-v2 = ON

        |
        v

GET /projects/payments/eval
?flag=checkout-v2
&user=alice

        |
        v

true
```

---

# 25. Example End-to-End Scenario

## Step 1: Create flag

```http
POST /projects/payments/flags
```

```json
{
  "name": "checkout-v2",
  "state": "ON"
}
```

Response:

```json
{
  "name": "checkout-v2",
  "state": "ON"
}
```

---

## Step 2: Evaluate

```http
GET /projects/payments/eval?flag=checkout-v2&user=alice
```

Result:

```json
{
  "enabled": true
}
```

---

## Step 3: Turn the flag OFF

```http
PUT /projects/payments/flags/checkout-v2
```

```json
{
  "state": "OFF"
}
```

---

## Step 4: Evaluate again

```http
GET /projects/payments/eval?flag=checkout-v2&user=alice
```

Result:

```json
{
  "enabled": false
}
```

---

# 26. Git History

The project is intentionally developed through meaningful commits.

Current progression:

```text
83e6cfe  first commit
    |
    v
3201083  Add project and feature flag persistence
    |
    v
3dacb5b  Implement feature flag CRUD APIs
    |
    v
2da8725  Implement feature flag evaluation
    |
    v
d07b6e8  Add feature flag evaluation API
```

The commit history represents the incremental development of the
application rather than putting the entire implementation into one
large commit.

---

# 27. Design Decisions

## Why project-scoped feature flags?

The service is multi-tenant.

Different applications/projects may use the same flag name.

For example:

```text
payments -> checkout-v2
mobile   -> checkout-v2
```

Therefore the project must be part of the feature flag identity.

---

## Why a database unique constraint?

Application-level checks alone are not sufficient.

The service performs:

```java
existsByProjectIdAndName(...)
```

before creating a flag.

But concurrent requests could still both pass that check.

The database constraint provides the final integrity guarantee:

```text
(project_id, name) UNIQUE
```

This is an important production-level consideration.

---

## Why use LAZY loading for Project?

The feature flag does not always need the complete Project object.

Therefore:

```java
@ManyToOne(fetch = FetchType.LAZY)
```

avoids unnecessarily loading the project whenever a feature flag is
retrieved.

---

# 28. Important Interview Concepts Demonstrated

This project can be used to discuss several backend engineering concepts.

### REST API design

- HTTP methods
- Resource-oriented URLs
- Path variables
- Query parameters
- HTTP status codes
- Request/response DTOs

### Spring Boot

- Dependency injection
- Controllers
- Services
- Repositories
- Configuration
- Validation

### JPA/Hibernate

- Entities
- Relationships
- Lazy loading
- Generated IDs
- Enum persistence
- Unique constraints

### Database design

- Primary keys
- Foreign keys
- Composite uniqueness
- Tenant isolation
- Data integrity

### Testing

- JUnit 5
- AssertJ
- Repository testing
- Integration testing
- Isolation testing
- Deterministic behavior testing

### Distributed-system concepts

The current service also establishes the foundation for more advanced
feature flag capabilities such as:

- Percentage rollouts
- User targeting
- Consistent hashing
- Caching
- Audit history
- Event-driven updates
- Horizontal scaling
- High availability

---

# 29. Production Improvements

The current implementation intentionally uses H2 and keeps the system
small.

For a production deployment, several areas could be improved.

## Database

Replace H2 with a production database such as PostgreSQL or MySQL.

---

## Migrations

Use:

```text
Flyway
```

or:

```text
Liquibase
```

instead of relying on:

```properties
spring.jpa.hibernate.ddl-auto=create-drop
```

---

## Caching

Feature flags are read much more frequently than they are changed.

A cache such as Redis could reduce database reads.

Possible architecture:

```text
Application
    |
    v
Feature Flag Service
    |
    +------> Redis
    |
    +------> Database
```

---

## Audit History

Production systems often need to know:

```text
Who changed the flag?
What changed?
When did it change?
What was the previous value?
```

An audit table could record:

```text
flag
old_state
new_state
changed_by
changed_at
```

---

## Percentage Rollouts

The deterministic hashing approach can be extended.

For example:

```text
hash(user + flag) % 100
```

could produce:

```text
0 - 9   -> enabled
10 - 99 -> disabled
```

This gives approximately a 10% rollout while keeping users in stable
buckets.

---

## Authentication and Authorization

A production service should authenticate callers and enforce permissions.

For example:

```text
Developer
    |
    v
Authentication
    |
    v
Authorization
    |
    v
Feature Flag API
```

A user should only be allowed to modify projects they are authorized to
access.

---

## Observability

Production deployment should include:

- Structured logging
- Metrics
- Distributed tracing
- Health checks
- Request latency monitoring
- Error-rate monitoring

---

# 30. Key Interview Questions

The project provides several useful interview discussion points.

### Q1. Why is `projectId` included in every feature flag lookup?

Because the application is multi-tenant and the same flag name can exist
in different projects.

---

### Q2. Why do we need a database unique constraint if we already call
`existsByProjectIdAndName()`?

Because application-level checks are vulnerable to race conditions.
The database constraint provides the final consistency guarantee.

---

### Q3. Why shouldn't DEFAULT use random values?

Because a user could receive inconsistent results across requests.

---

### Q4. How do you guarantee a stable DEFAULT result?

Use deterministic hashing based on stable inputs such as:

```text
flag + user
```

---

### Q5. Why use DTOs instead of exposing entities directly?

DTOs decouple the REST API contract from the persistence model.

This prevents database implementation details from becoming API contracts.

---

### Q6. Why use a service layer?

It keeps business logic outside controllers and provides a clean boundary
for future changes.

---

### Q7. What does `@ManyToOne(fetch = FetchType.LAZY)` mean?

A feature flag belongs to one project, but the Project entity is loaded
lazily instead of immediately.

---

### Q8. How does tenant isolation work?

Tenant/project ID is included in feature flag queries:

```text
projectId + flagName
```

and the database also enforces uniqueness using:

```text
(project_id, name)
```

---

### Q9. How would you implement a 10% rollout?

Hash a stable user/flag combination into a bucket:

```text
hash(user + flag) % 100
```

Then enable the feature for buckets:

```text
0-9
```

This keeps the same users consistently enabled.

---

### Q10. What happens if two requests create the same flag concurrently?

Both requests might initially pass the existence check, but the database
unique constraint ensures that only one can successfully persist the
duplicate `(project_id, name)` combination.

---

# 31. Future Roadmap

Possible future iterations of the project:

```text
Current
  |
  +-- CRUD APIs
  |
  +-- Evaluation API
  |
  +-- Multi-tenant isolation
  |
  +-- Deterministic DEFAULT
  |
  v
Percentage Rollouts
  |
  v
User Targeting
  |
  v
Redis Caching
  |
  v
Audit Logging
  |
  v
Authentication / Authorization
  |
  v
PostgreSQL
  |
  v
Flyway Migrations
  |
  v
Kafka/Event-driven cache invalidation
  |
  v
Production-ready Feature Flag Platform
```

---

# 32. Summary

This project implements a small but realistic feature flag service.

The core concepts are:

```text
Project
   |
   +---- Feature Flags
              |
              +---- ON
              +---- OFF
              +---- DEFAULT
```

The service exposes REST APIs for:

```text
CREATE
READ
UPDATE
DELETE
EVALUATE
```

The most important architectural properties are:

1. Feature flags are scoped to projects.
2. Feature names are unique within a project.
3. Cross-project access is isolated.
4. ON always evaluates to true.
5. OFF always evaluates to false.
6. DEFAULT produces a deterministic result.
7. Business logic lives in the service layer.
8. Persistence logic lives in repositories.
9. API contracts use DTOs.
10. Automated tests verify the important behavior.

The current implementation provides a strong foundation for extending the
service toward percentage rollouts, caching, audit logging,
authentication, event-driven updates, and production deployment.