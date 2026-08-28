# Development Notes

## Assignment

Feature Flag Service take-home exercise.

The goal is to build a small backend service that allows applications
to create, manage, and evaluate feature flags for multiple projects/tenants.

The important requirements are:

- CRUD for feature flags
- Feature flag evaluation
- Multi-tenancy
- Strict tenant isolation
- Stable evaluation for a given flag and user
- ON / OFF / DEFAULT flag states
- Automated tests
- Client usage example
- README
- One-page engineering write-up
- Meaningful Git commit history

---

# Technology Choice

## Chosen stack

- Java 17
- Spring Boot 4.1.1
- Spring Data JPA
- H2
- Maven
- JUnit
- AssertJ

## Why this stack

Java 17 was already available in the development environment, so
it was a practical choice.

Spring Boot provides the REST API and application framework.

Spring Data JPA provides persistence with minimal boilerplate while
still allowing the tenant-scoped query requirements to be expressed
clearly.

H2 was selected because the assignment is a small take-home exercise
and does not require an external database setup.

---

# Development Progress

## Commit 1 - Project skeleton

Git commit:

`83e6cfe first commit`

Started the project using Spring Initializr.

The initial project contains:

- Spring Boot application
- Maven wrapper
- Java 17 configuration
- H2
- Spring Data JPA
- Spring Web
- Validation
- Initial Spring Boot test

Verified that the application could start successfully.

---

# Commit 2 - Project and Feature Flag Persistence

Git commit:

`3201083 Add project and feature flag persistence`

The domain model was designed around two main entities:

## Project

Represents a tenant/project using the feature flag service.

A project has:

- project ID
- project name

## FeatureFlag

Represents a feature flag belonging to a project.

A feature flag has:

- ID
- name
- state
- project

The relationship is:

`Project -> FeatureFlag`

A feature flag therefore belongs to a specific project.

---

## Flag State

The feature flag state is represented using an enum:

- `ON`
- `OFF`
- `DEFAULT`

The enum is persisted using:

`@Enumerated(EnumType.STRING)`

Using strings rather than ordinal enum values avoids coupling the
database representation to the enum declaration order.

---

## Tenant Isolation Decision

Feature flags are queried using both:

- project ID
- flag name

For example:

`findByProjectIdAndName(projectId, name)`

instead of querying by flag name alone.

This is intentional because the same flag name can exist in
multiple projects.

Example:

Project A:

`checkout-v2`

Project B:

`checkout-v2`

These are two different flags.

The project ID must therefore be part of every feature flag lookup.

This tenant-scoped lookup pattern is also used by the evaluation logic.

---

## Database Constraint

A database-level unique constraint was added for:

`project_id + name`

Constraint:

`uk_feature_flag_project_name`

This means a project cannot contain two feature flags with the same name,
while different projects can still use the same flag name.

For example:

Allowed:

- `payments / checkout-v2`
- `mobile / checkout-v2`

Not allowed:

- `payments / checkout-v2`
- `payments / checkout-v2`

The database constraint provides an additional layer of protection
beyond the service-level duplicate check.

---

# Repository

Created `FeatureFlagRepository` using Spring Data JPA.

Important methods:

- `findByProjectIdAndName`
- `findAllByProjectId`
- `existsByProjectIdAndName`

Spring Data derives the queries from the method names.

The repository methods intentionally include `projectId` so that
tenant isolation is enforced at the data-access layer.

---

# Testing at Commit 2

Created repository tests covering:

1. Finding a flag for a project.
2. Preventing a project from finding another project's flag.
3. Checking whether a flag exists for a project.

The original Spring Boot context test was also retained.

Total tests at this checkpoint:

`4`

Result:

`Tests run: 4`
`Failures: 0`
`Errors: 0`
`Skipped: 0`

Build result:

`BUILD SUCCESS`

---

# Important Issue Encountered

Initially the repository contained:

`existByProjectIdAndName`

instead of:

`existsByProjectIdAndName`

Spring Data JPA interpreted the method name as a property lookup and
failed during application context creation.

The error indicated that no property named `existByProjectId` could be
found on `FeatureFlag`.

The method was corrected to:

`existsByProjectIdAndName`

After the correction, the tests passed.

This validated that Spring Data repository method names are parsed and
validated when the application context starts.

---

# Spring Boot 4 Testing Issue

While creating the repository tests, the initial import used the older
`DataJpaTest` package:

`org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest`

The project uses Spring Boot 4.1.1.

The dependency tree confirmed that the JPA testing support was already
available through the Spring Boot 4 testing dependency.

The correct Spring Boot 4 package was then used:

`org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`

No framework downgrade was necessary.

The tests subsequently compiled and passed.

---

# Commit 3 - Feature Flag CRUD APIs

Git commit:

`3dacb5b Implement feature flag CRUD APIs`

The persistence layer was extended with a service layer and REST
controller.

The following operations were implemented:

- Create a feature flag
- Get all flags for a project
- Get a specific flag
- Update a flag
- Delete a flag

The controller uses:

`/projects/{projectId}/flags`

This keeps the project/tenant context visible in the API URL.

---

## API Endpoints

### Create

`POST /projects/{projectId}/flags`

Example:

```json
{
  "name": "checkout-v2",
  "state": "ON"
}