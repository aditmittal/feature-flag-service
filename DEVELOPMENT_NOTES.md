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
- Automated tests
- Client usage example
- README
- One-page engineering write-up
- Meaningful Git commit history

---

## Technology Choice

### Chosen stack

- Java 17
- Spring Boot 4.1.1
- Spring Data JPA
- H2
- Maven
- JUnit
- AssertJ

### Why this stack

Java 17 was already available in the development environment, so
it was a practical choice.

Spring Boot provides the REST API and application framework, while
Spring Data JPA provides persistence with minimal boilerplate.

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

## Persistence Layer

The domain model was designed around two main entities:

### Project

Represents a tenant/project using the feature flag service.

A project has:

- project ID
- project name
- feature flags

### FeatureFlag

Represents a feature flag belonging to a project.

A feature flag has:

- ID
- name
- state
- project

The relationship is:

Project -> FeatureFlags

A feature flag therefore belongs to a specific project.

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

The project ID must therefore be part of every lookup.

---

## Repository

Created `FeatureFlagRepository` using Spring Data JPA.

Important methods:

- `findByProjectIdAndName`
- `existsByProjectIdAndName`

Spring Data derives the queries from the method names.

---

# Testing

Created repository tests covering:

1. Finding a flag for a project.
2. Preventing a project from finding another project's flag.
3. Checking whether a flag exists for a project.

The original Spring Boot context test is also retained.

Total tests at this checkpoint:

4

Result:

Tests run: 4  
Failures: 0  
Errors: 0  
Skipped: 0

Build result:

BUILD SUCCESS

---

# Important Issue Encountered

Initially the repository contained:

`existByProjectIdAndName`

instead of:

`existsByProjectIdAndName`

Spring Data JPA interpreted the method name as a property lookup and
failed during application context creation.

The error was:

`No property 'existByProjectId' found for type 'FeatureFlag'`

The method was corrected to:

`existsByProjectIdAndName`

After the correction, the tests passed.

This was a useful validation that Spring Data repository method names
are parsed and validated when the application context starts.

---

# Spring Boot 4 Testing Issue

While creating the repository tests, the initial import used the older
`DataJpaTest` package:

`org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest`

The project uses Spring Boot 4.1.1.

The dependency tree confirmed that the JPA testing support was already
present through:

`spring-boot-starter-data-jpa-test`

The correct Spring Boot 4 package was then used:

`org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest`

No framework downgrade was necessary.

The tests subsequently compiled and passed.

---

# Git Checkpoint 2

Git commit:

`3201083 Add project and feature flag persistence`

This checkpoint was pushed to GitHub.

Current Git history:

- `3201083 Add project and feature flag persistence`
- `83e6cfe first commit`

The working tree is clean and the branch is synchronized with
the remote repository.

---
