# Residual Risk Calculator — Backend

AI-powered Residual Risk Calculator built with Spring Boot 3.x, PostgreSQL, Redis, and JWT authentication.

---

## Tech Stack

| Layer        | Technology                        |
|--------------|-----------------------------------|
| Language     | Java 17                           |
| Framework    | Spring Boot 3.2.x                 |
| Database     | PostgreSQL 16                     |
| Cache        | Redis 7                           |
| Security     | Spring Security + JWT (JJWT 0.12) |
| Migrations   | Flyway                            |
| Docs         | SpringDoc OpenAPI (Swagger UI)    |
| Build        | Maven 3.9                         |
| Container    | Docker + Docker Compose           |

---

## Project Structure

```
src/main/java/com/internship/tool/
├── controller/          # REST controllers
├── service/             # Business logic
├── repository/          # JPA repositories
├── entity/              # JPA entities
├── dto/                 # Request/Response DTOs
├── config/              # Security, JWT, Redis, OpenAPI
└── exception/           # Custom exceptions + global handler
src/main/resources/
├── application.yml
└── db/migration/        # Flyway SQL scripts
```

---

## Quick Start

### Option 1 — Docker Compose (Recommended)

```bash
# 1. Copy environment file
cp .env.example .env

# 2. Edit .env with your values (especially JWT_SECRET and mail settings)

# 3. Build and start all services
docker-compose up --build

# 4. Application is available at http://localhost:8080
```

### Option 2 — Local Development

**Prerequisites:** Java 17, Maven 3.9+, PostgreSQL 16, Redis 7

```bash
# 1. Create database
psql -U postgres -c "CREATE DATABASE risk_calculator;"

# 2. Set environment variables (or update application.yml)
export DB_URL=jdbc:postgresql://localhost:5432/risk_calculator
export DB_USER=postgres
export DB_PASSWORD=postgres
export REDIS_HOST=localhost
export JWT_SECRET=bXktc3VwZXItc2VjcmV0LWtleS1mb3ItcHJvZHVjdGlvbi11c2Utb25seS0yNTZiaXQ=

# 3. Run the application
mvn spring-boot:run
```

---

## API Documentation

Swagger UI: **http://localhost:8080/swagger-ui.html**

---

## Authentication

All endpoints (except `/api/auth/**`) require a Bearer JWT token.

### Register

```http
POST /api/auth/register
Content-Type: application/json

{
  "username": "johndoe",
  "email": "john@example.com",
  "password": "secret123"
}
```

### Login

```http
POST /api/auth/login
Content-Type: application/json

{
  "username": "johndoe",
  "password": "secret123"
}
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiJ9...",
  "type": "Bearer",
  "username": "johndoe",
  "email": "john@example.com",
  "role": "USER"
}
```

Use the token in subsequent requests:
```
Authorization: Bearer <token>
```

---

## Risk API Endpoints

| Method | Endpoint                  | Role        | Description              |
|--------|---------------------------|-------------|--------------------------|
| GET    | /api/risks                | USER, ADMIN | Get all risks (paginated)|
| GET    | /api/risks/{id}           | USER, ADMIN | Get risk by ID           |
| GET    | /api/risks/search?q=      | USER, ADMIN | Search risks             |
| POST   | /api/risks                | USER, ADMIN | Create a new risk        |
| PUT    | /api/risks/{id}           | USER, ADMIN | Update a risk            |
| DELETE | /api/risks/{id}           | ADMIN only  | Soft delete a risk       |

### Sample Requests

**Create Risk**
```http
POST /api/risks
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "SQL Injection Vulnerability",
  "description": "Unsanitized user input in login form",
  "category": "Security",
  "riskScore": 85.5
}
```

**Get All Risks (paginated)**
```http
GET /api/risks?page=0&size=10&sortBy=riskScore&direction=desc
Authorization: Bearer <token>
```

**Search Risks**
```http
GET /api/risks/search?q=security&page=0&size=10
Authorization: Bearer <token>
```

**Update Risk**
```http
PUT /api/risks/1
Authorization: Bearer <token>
Content-Type: application/json

{
  "name": "SQL Injection - Updated",
  "description": "Fixed with parameterized queries",
  "category": "Security",
  "riskScore": 20.0
}
```

**Delete Risk (Admin only)**
```http
DELETE /api/risks/1
Authorization: Bearer <token>
```

---

## Default Admin Account

After first startup, a default admin is seeded:

| Field    | Value                      |
|----------|----------------------------|
| Username | `admin`                    |
| Password | `Admin@123`                |
| Role     | `ADMIN`                    |

---

## Running Tests

```bash
mvn test
```

Tests cover:
- `JwtUtilTest` — token generation, extraction, validation
- `RiskServiceTest` — all CRUD operations with Mockito
- `AuthServiceTest` — register, login, duplicate checks
- `RiskControllerTest` — REST layer with MockMvc

---

## Environment Variables

| Variable        | Default                          | Description                    |
|-----------------|----------------------------------|--------------------------------|
| DB_URL          | jdbc:postgresql://localhost:5432/risk_calculator | PostgreSQL URL |
| DB_USER         | postgres                         | DB username                    |
| DB_PASSWORD     | postgres                         | DB password                    |
| REDIS_HOST      | localhost                        | Redis host                     |
| REDIS_PORT      | 6379                             | Redis port                     |
| REDIS_PASSWORD  | (empty)                          | Redis password                 |
| JWT_SECRET      | (required)                       | Base64-encoded 256-bit key     |
| JWT_EXPIRATION  | 86400000                         | Token TTL in ms (24h)          |
| MAIL_HOST       | smtp.gmail.com                   | SMTP host                      |
| MAIL_PORT       | 587                              | SMTP port                      |
| MAIL_USERNAME   | (required for email)             | SMTP username                  |
| MAIL_PASSWORD   | (required for email)             | SMTP password / app password   |
| ADMIN_EMAIL     | admin@riskcalculator.com         | Daily summary recipient        |
| SERVER_PORT     | 8080                             | Application port               |

---

## Generating a JWT Secret

```bash
# Generate a secure Base64-encoded 256-bit key
openssl rand -base64 32
```

Paste the output as your `JWT_SECRET` value.
