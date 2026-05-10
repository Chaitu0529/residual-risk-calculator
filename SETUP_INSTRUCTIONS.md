# Setup Instructions for Demo Day

## Prerequisites

- Docker Desktop installed and running
- Git installed
- Postman (optional, for API testing)

---

## Step 1: Clone and Setup

```bash
# Clone the repository
git clone <your-repo-url>
cd residual-risk-calculator

# Copy environment file
cp .env.example .env
```

---

## Step 2: Configure Environment Variables

Edit `.env` file with your settings:

```bash
# Database (defaults work for Docker)
DB_USER=postgres
DB_PASSWORD=postgres

# JWT Secret — generate a real one:  openssl rand -base64 32
JWT_SECRET=bXktc3VwZXItc2VjcmV0LWtleS1mb3ItcHJvZHVjdGlvbi11c2Utb25seS0yNTZiaXQ=

# Email (Optional - for live notifications during demo)
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password
ADMIN_EMAIL=admin@riskcalculator.com
```

**Generate a secure JWT secret:**
```bash
openssl rand -base64 32
```

---

## Step 3: Start the Application

```bash
# Build and start all services (PostgreSQL, Redis, Backend)
docker-compose up --build

# Wait for the application to start (about 30-60 seconds)
# You should see: "Started RiskCalculatorApplication"
```

---

## Step 4: Verify the Application

### Check Health
```bash
curl http://localhost:8080/actuator/health
# Expected: {"status":"UP"}
```

### Access Swagger UI
Open in browser: **http://localhost:8080/swagger-ui.html**

---

## Step 5: Test the APIs

### Option A: Using Postman

1. Import `postman_collection.json` into Postman
2. Run "Login as Admin" request (username: `admin`, password: `Admin@123`)
3. Token is automatically saved to collection variables
4. Test other endpoints — all pre-configured

### Option B: Using cURL

**1. Login as Admin**
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "admin", "password": "Admin@123"}'
```

Copy the `token` from the response.

**2. Get All Risks (18 seeded records ready)**
```bash
curl -X GET "http://localhost:8080/api/risks?page=0&size=10&sortBy=riskScore&direction=desc" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**3. Search Risks**
```bash
curl -X GET "http://localhost:8080/api/risks/search?q=security" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE"
```

**4. Create a Risk**
```bash
curl -X POST http://localhost:8080/api/risks \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  -d '{
    "name": "New Vulnerability Found",
    "description": "Discovered during penetration test",
    "category": "Security",
    "riskScore": 77.5
  }'
```

**5. Test RBAC — Register a USER and try to delete (should get 403)**
```bash
# Register a regular user
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"username": "demouser", "email": "demo@example.com", "password": "demo1234"}'

# Try to delete with USER token — expect 403 Forbidden
curl -X DELETE http://localhost:8080/api/risks/1 \
  -H "Authorization: Bearer USER_TOKEN_HERE"
```

---

## Step 6: Run Tests with Coverage

```bash
# Run all tests + generate JaCoCo coverage report
mvn verify

# View HTML coverage report in browser
# Windows:
start target\site\jacoco\index.html
# Mac/Linux:
open target/site/jacoco/index.html
```

Coverage thresholds enforced (build fails if below):
- **Line coverage: ≥ 80%**
- **Branch coverage: ≥ 70%**

---

## Troubleshooting

### Port Already in Use
```bash
# Change the port in .env
SERVER_PORT=8081
```

### Database Connection Issues
```bash
docker ps | grep postgres
docker logs risk_postgres
```

### Redis Connection Issues
```bash
docker exec -it risk_redis redis-cli ping
# Expected: PONG
```

### Application Logs
```bash
docker logs -f risk_backend
```

---

## Stopping the Application

```bash
# Stop all services
docker-compose down

# Stop and remove volumes (full clean slate)
docker-compose down -v
```

---

## Demo Day Checklist

### Before Demo
- [ ] `docker-compose up --build` completes without errors
- [ ] `curl http://localhost:8080/actuator/health` returns `{"status":"UP"}`
- [ ] Swagger UI loads at http://localhost:8080/swagger-ui.html
- [ ] `mvn verify` passes with ≥80% coverage

### During Demo
- [ ] Login as admin → show JWT token with `"type": "Bearer"`
- [ ] GET /api/risks → show 18 seeded risks, paginated
- [ ] Search `?q=compliance` → filtered results
- [ ] POST a new risk → 201 Created
- [ ] PUT to update → 200 OK, cache evicted
- [ ] DELETE as admin → 204 No Content
- [ ] Register a USER, try DELETE → 403 Forbidden (RBAC proof)
- [ ] Hit GET twice → second call instant (Redis cache hit)
- [ ] Show `docker ps` → all 3 containers healthy

---

## Default Credentials

| Username | Password   | Role  |
|----------|------------|-------|
| admin    | Admin@123  | ADMIN |

---

## Seed Data Summary

18 realistic risks seeded across 6 categories:

| Category | Count | Score Range |
|---|---|---|
| Security | 5 | 55 – 92.5 |
| Compliance | 3 | 62 – 81 |
| Operational | 4 | 40 – 85 |
| Financial | 2 | 52 – 70 |
| Reputational | 2 | 65 – 78 |
| Technical Debt | 2 | 35 – 60 |

Plus 1 soft-deleted risk (score 95) to demonstrate soft delete.

---

## API Endpoints Summary

| Method | Endpoint              | Auth Required | Role        |
|--------|-----------------------|---------------|-------------|
| POST   | /api/auth/register    | No            | —           |
| POST   | /api/auth/login       | No            | —           |
| GET    | /api/risks            | Yes           | USER, ADMIN |
| GET    | /api/risks/{id}       | Yes           | USER, ADMIN |
| GET    | /api/risks/search?q=  | Yes           | USER, ADMIN |
| POST   | /api/risks            | Yes           | USER, ADMIN |
| PUT    | /api/risks/{id}       | Yes           | USER, ADMIN |
| DELETE | /api/risks/{id}       | Yes           | ADMIN only  |

---

## Test Classes Summary

| Class | Tests | What It Covers |
|---|---|---|
| `JwtUtilTest` | 5 | Token generation, extraction, validation, expiry |
| `RiskServiceTest` | 11 | All CRUD, search, soft delete, field mapping |
| `AuthServiceTest` | 5 | Register, login, duplicates, bad credentials |
| `RiskControllerTest` | 8 | All endpoints, role access, validation |
| `AuthControllerTest` | 6 | Register/login success, 400/401/409 errors |
| `GlobalExceptionHandlerTest` | 6 | All exception types → correct HTTP status |
| `EmailServiceTest` | 6 | All email methods, graceful failure handling |
| **Total** | **47** | Full layer coverage |
