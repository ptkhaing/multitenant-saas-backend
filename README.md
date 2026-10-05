# Multi-Tenant SaaS Backend

A production-shaped backend for a multi-tenant SaaS platform, built with Java and Spring Boot. Implements JWT authentication and fine-grained role-based access control (RBAC), backed by PostgreSQL and deployed as a Docker container.

**[Live Demo](https://multitenant-saas-backend-5nxx.onrender.com)**
*(Free-tier hosting — the app may take 30–60 seconds to respond on the first request after a period of inactivity.)*

## Overview

This project models a common SaaS pattern: multiple independent organizations ("tenants") sharing one application, each with their own users, roles, and data — while keeping that data properly isolated and access properly controlled.

Rather than a single fixed set of permissions, each tenant can define its own roles (e.g. "Admin", "Editor") and attach specific permissions to them. Users are then assigned roles, and API endpoints check for specific permissions before allowing an action — enforced declaratively at the endpoint level, not scattered through business logic.

## Features

- **JWT authentication** — stateless signup/login with BCrypt-hashed passwords
- **Multi-tenancy** — all core data scoped to a tenant
- **Role-based access control** — permissions attach to roles, roles attach to users; endpoints declare required permissions via `@PreAuthorize`
- **Relational data model** — tenants, users, roles, permissions, and a protected `Project` resource, with proper foreign keys and join tables
- **Dockerized** — multi-stage build producing a lightweight runtime image
- **Deployed** — live on Render, connected to a managed PostgreSQL instance

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4 (Web, Data JPA, Security) |
| Database | PostgreSQL |
| Auth | JWT (jjwt), BCrypt |
| Build | Maven |
| Containerization | Docker (multi-stage build) |
| Hosting | Render |

## Data Model

- **Tenant** — an organization using the platform
- **UserAccount** — belongs to a tenant
- **Role** — tenant-scoped (each tenant defines its own roles)
- **Permission** — a global, reusable capability (e.g. `PROJECT_CREATE`)
- **Project** — a sample protected resource, used to demonstrate RBAC enforcement in practice

Roles and permissions are joined many-to-many, as are users and roles — so a user's effective permissions are the union of every permission attached to every role they hold.

## API Overview

| Endpoint | Method | Auth required | Description |
|---|---|---|---|
| `/api/auth/signup` | POST | No | Create a new user under a tenant |
| `/api/auth/login` | POST | No | Authenticate and receive a JWT |
| `/api/auth/users/{userId}/roles/{roleId}` | POST | Yes | Assign a role to a user |
| `/api/tenants` | GET / POST | Yes | List / create tenants |
| `/api/roles` | GET / POST | Yes | List / create roles |
| `/api/roles/{roleId}/permissions/{permissionId}` | POST | Yes | Attach a permission to a role |
| `/api/permissions` | GET / POST | Yes | List / create permissions |
| `/api/projects` | GET | Yes | List projects |
| `/api/projects` | POST | Yes, `PROJECT_CREATE` | Create a project |
| `/api/projects/{id}` | DELETE | Yes, `PROJECT_DELETE` | Delete a project |

All authenticated requests use `Authorization: Bearer <token>`.

## Running Locally

### Prerequisites
- Java 21
- Maven
- PostgreSQL running locally

### Setup

```bash
# Create the database
createdb multitenant_saas_db

# Run the app
./mvnw spring-boot:run
```

By default the app connects to `jdbc:postgresql://localhost:5432/multitenant_saas_db`. Override via environment variables (`SPRING_DATASOURCE_URL`, `SPRING_DATASOURCE_USERNAME`, `SPRING_DATASOURCE_PASSWORD`, `JWT_SECRET`, `JWT_EXPIRATION`) as needed.

### Running with Docker

```bash
docker build -t multitenant-saas-backend .
docker run -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:postgresql://host.docker.internal:5432/multitenant_saas_db \
  multitenant-saas-backend
```

## Example: Testing the RBAC Flow

```bash
# 1. Sign up a user under an existing tenant
curl -X POST http://localhost:8080/api/auth/signup \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123","firstName":"Test","lastName":"User","tenantId":"<tenant-uuid>"}'

# 2. Log in to receive a JWT
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"user@example.com","password":"password123"}'

# 3. Attempt a protected action — fails until the user has the right permission
curl -X POST http://localhost:8080/api/projects \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{"name":"New Project","tenantId":"<tenant-uuid>"}'
```

## Notes

This project was built to demonstrate backend and security fundamentals in a stack (Java/Spring Boot) distinct from other projects in this portfolio, which use Node.js/Express. It covers authentication, authorization, relational data modeling, and containerized deployment end to end.
