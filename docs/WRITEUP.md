# Feature Flag Service — Engineering Take-Home Write-Up

## 1. What did you ask the AI to do, and what did you write or decide yourself?

I used an AI assistant primarily as a development partner for designing and implementing the
feature flag service. I asked it to help break the assignment into manageable steps, suggest
a Spring Boot project structure, design the JPA entities and repositories, implement the CRUD
APIs, implement feature flag evaluation, and suggest automated tests.

I reviewed and ran the generated code rather than treating the AI output as final. I made the
main architectural decisions around the domain model, API structure, tenant/project scoping,
database constraints, evaluation behavior, and test scenarios.

The service is structured around Projects and FeatureFlags. A feature flag belongs to exactly
one project, and the combination of project and flag name is unique. This makes the tenant
boundary explicit in both the object model and database schema.

The evaluation API was designed as a project-scoped operation so that a caller cannot evaluate
a flag without specifying the tenant it belongs to.

## 2. Where did you override, correct, or throw away the AI's output — and why?

During implementation, I reviewed and corrected several issues instead of blindly accepting the
generated code.

One important issue was database initialization. Because the application uses an in-memory H2
database, data disappears whenever the application restarts. I added `data.sql` so that the
application has predictable initial project and feature-flag data.

There was also an issue where tests initially interacted with the application's database
initialization data. This caused duplicate-key and missing-project failures because test data
and initialization data were sharing the same database context. I separated test configuration
from the normal application configuration so that tests have an isolated and deterministic
environment.

I also verified the API behavior manually using Postman rather than relying only on unit and
repository tests. This exposed issues such as missing project records and incorrect endpoint
paths, which were then corrected.

## 3. Biggest trade-offs

### H2 instead of PostgreSQL/MySQL

I chose H2 because the exercise is intentionally small and H2 makes the project easy to run
without requiring an external database.

The alternative would be PostgreSQL, which would be more representative of a production
deployment but would introduce additional setup requirements. For this take-home exercise,
simplicity and reproducibility were more valuable.

### Deterministic hash-based default evaluation

For a DEFAULT flag, the evaluation uses a deterministic function of the flag name and user.
This means the same user and flag consistently receive the same result without storing a
per-user assignment.

The alternative would be storing explicit user assignments or using a more sophisticated
percentage rollout mechanism. That would be more flexible but unnecessarily complex for the
requirements of this exercise.

### Database-enforced tenant isolation

Feature flags are queried using both `projectId` and `name`, and the database has a unique
constraint on `(project_id, name)`.

An alternative would be relying entirely on application-level checks. The database constraint
provides an additional integrity guarantee and makes the tenant boundary part of the persistence
model.

## 4. What's missing, or what would you do with another day?

With another day, I would improve the production-readiness of the service.

I would add:

- PostgreSQL integration tests using Testcontainers.
- More complete controller-level integration tests.
- Standardized error responses using `@ControllerAdvice`.
- API documentation using OpenAPI/Swagger.
- Validation for project and flag names.
- Authentication and authorization between tenants.
- Percentage-based rollout support.
- Better observability with structured logging and metrics.
- Docker support and a CI pipeline.
- More comprehensive concurrency tests around flag creation and updates.

For a production system, I would also consider caching frequently evaluated flags because
evaluation is likely to be a high-read operation.

The current implementation intentionally stays small and focused on the requirements of the
exercise while keeping the design extensible.