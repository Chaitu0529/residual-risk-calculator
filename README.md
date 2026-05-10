# Residual Risk Calculator

AI-powered Residual Risk Calculator backend developed using Spring Boot, PostgreSQL, Redis, JWT Authentication, Docker, and Swagger.

## Features

- JWT Authentication
- Risk CRUD APIs
- PostgreSQL Integration
- Redis Caching
- Dockerized Deployment
- Swagger API Documentation
- Spring Boot Actuator Health Monitoring

---

# Tech Stack

- Java 17
- Spring Boot 3
- PostgreSQL
- Redis
- Docker
- JWT
- Flyway
- Swagger/OpenAPI

---

# Running the Project

## Start Application

```bash
docker compose up --build
```

---

# API Documentation

Swagger UI:

```txt
http://localhost:8080/swagger-ui/index.html
```

---

# Health Endpoint

```txt
http://localhost:8080/actuator/health
```

Expected response:

```json
{
  "status": "UP"
}
```

---

# Authentication APIs

- POST `/api/auth/register`
- POST `/api/auth/login`

---

# Risk APIs

- GET `/api/risks`
- GET `/api/risks/{id}`
- POST `/api/risks`
- PUT `/api/risks/{id}`
- DELETE `/api/risks/{id}`

---

# Docker Services

- Spring Boot Backend
- PostgreSQL
- Redis

---

# Security

- JWT Authentication
- BCrypt Password Encryption
- Protected APIs

---

# Author

Developed as part of the Residual Risk Calculator Capstone Project.
