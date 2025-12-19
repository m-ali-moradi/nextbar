# JWT Authentication Guide

## Overview
Your microservices architecture now uses JWT (JSON Web Token) authentication for securing endpoints. The bar-service, warehouse-service, and event-planner-service all require valid JWT tokens to access their APIs.

## Architecture

```
┌─────────┐      ┌────────────┐      ┌───────────────┐
│ Client  │─────▸│  Gateway   │─────▸│ Users Service │
└─────────┘      │   :8080    │      │     :8090     │
                 └────────────┘      └───────────────┘
                       │                     │
                       │              (JWT Generation)
                       │
          ┌────────────┴────────────┐
          │                         │
   ┌──────▼──────┐         ┌────────▼─────────┐
   │ Bar Service │         │ Warehouse Service│
   │    :8081    │         │      :8085       │
   └─────────────┘         └──────────────────┘
    (JWT Required)          (JWT Required)
```

## Step-by-Step: Getting and Using a JWT Token

### Step 1: Start All Services

Run the startup script:
```powershell
.\start-all-services.ps1
```

Wait ~30 seconds for all services to start. The script will open 4 terminal windows:
- Eureka Server (8761)
- Config Server (8888)  
- API Gateway (8080)
- Users Service (8090)

### Step 2: Register a User (First Time Only)

```powershell
$registerBody = @{
    username = "admin"
    password = "Admin123!"
    email = "admin@example.com"
    role = "ADMIN"
} | ConvertTo-Json

Invoke-RestMethod -Uri "http://localhost:8080/api/users/auth/register" `
    -Method Post `
    -Body $registerBody `
    -ContentType "application/json"
```

**Response:**
```json
{
  "id": 1,
  "username": "admin",
  "email": "admin@example.com",
  "role": "ADMIN"
}
```

### Step 3: Login to Get JWT Token

```powershell
$loginBody = @{
    username = "admin"
    password = "Admin123!"
} | ConvertTo-Json

$response = Invoke-RestMethod -Uri "http://localhost:8080/api/users/auth/login" `
    -Method Post `
    -Body $loginBody `
    -ContentType "application/json"

# Save the token for later use
$token = $response.accessToken
Write-Host "JWT Token: $token"
```

**Response:**
```json
{
  "accessToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYzOTU...",
  "refreshToken": "eyJhbGciOiJIUzUxMiJ9.eyJzdWIiOiJhZG1pbiIsImlhdCI6MTYzOTU...",
  "tokenType": "Bearer",
  "expiresIn": 3600
}
```

### Step 4: Use Token to Access Protected Endpoints

#### Example: Get all bars from bar-service

```powershell
$headers = @{
    "Authorization" = "Bearer $token"
}

Invoke-RestMethod -Uri "http://localhost:8080/api/bars" `
    -Method Get `
    -Headers $headers
```

#### Example: Get warehouse inventory

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/warehouse/inventory" `
    -Method Get `
    -Headers $headers
```

#### Example: Get events from event-planner

```powershell
Invoke-RestMethod -Uri "http://localhost:8080/api/events" `
    -Method Get `
    -Headers $headers
```

## Using the Test Script

We've created a convenient test script that automates the entire flow:

```powershell
.\test-jwt-auth.ps1
```

This script will:
1. Register a test user (if not exists)
2. Login and get a JWT token
3. Test the bar-service with the token
4. Display the token for manual testing

## Curl Examples

### Register
```bash
curl -X POST http://localhost:8080/api/users/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin123!",
    "email": "admin@example.com",
    "role": "ADMIN"
  }'
```

### Login
```bash
curl -X POST http://localhost:8080/api/users/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin",
    "password": "Admin123!"
  }'
```

### Access Bar Service (with token)
```bash
curl -H "Authorization: Bearer YOUR_TOKEN_HERE" \
  http://localhost:8080/api/bars
```

## Token Details

- **Token Type**: Bearer
- **Algorithm**: HS512 (HMAC with SHA-512)
- **Access Token Expiration**: 1 hour (3600 seconds)
- **Refresh Token Expiration**: 24 hours (86400 seconds)
- **Secret**: Configured in application.yml (shared across all services)

## Service Endpoints

### Users Service (:8090)
- `POST /auth/register` - Register new user (public)
- `POST /auth/login` - Login and get JWT token (public)
- `POST /auth/refresh` - Refresh access token
- `GET /users` - Get all users (protected)
- `GET /users/{id}` - Get user by ID (protected)

### Bar Service (:8081)
All endpoints require JWT token:
- `GET /bars` - Get all bars
- `GET /bars/{id}` - Get bar by ID
- `POST /bars` - Create new bar
- `PUT /bars/{id}` - Update bar
- `DELETE /bars/{id}` - Delete bar
- `GET /bars/{barId}/products` - Get products for a bar

### Warehouse Service (:8085)
All endpoints require JWT token:
- `GET /inventory` - Get all inventory
- `GET /inventory/{id}` - Get inventory item
- `POST /inventory` - Create inventory item
- `PUT /inventory/{id}` - Update inventory
- `DELETE /inventory/{id}` - Delete inventory item

### Event Planner Service (:8082)
All endpoints require JWT token:
- `GET /events` - Get all events
- `GET /events/{id}` - Get event by ID
- `POST /events` - Create new event
- `PUT /events/{id}` - Update event
- `DELETE /events/{id}` - Delete event

## Public Endpoints (No Token Required)

The following endpoints are publicly accessible:
- `/actuator/**` - Health checks and metrics
- `/h2-console/**` - H2 database console (development only)
- `/swagger-ui/**` - API documentation
- `/v3/api-docs/**` - OpenAPI specification
- `/auth/register` - User registration
- `/auth/login` - User login

## Troubleshooting

### "Access Denied" or "401 Unauthorized"
**Solution**: Make sure you're including the JWT token in the `Authorization` header with `Bearer` prefix:
```
Authorization: Bearer eyJhbGciOiJIUzUxMiJ9...
```

### "403 Forbidden"
**Solution**: Your token is valid but you don't have permission. Check your user role.

### "Token Expired"
**Solution**: Login again to get a new token, or use the refresh token:
```powershell
$refreshBody = @{ refreshToken = $response.refreshToken } | ConvertTo-Json
Invoke-RestMethod -Uri "http://localhost:8080/api/users/auth/refresh" `
    -Method Post `
    -Body $refreshBody `
    -ContentType "application/json"
```

### Services Not Responding
**Solution**: 
1. Check all services are running: `netstat -an | findstr "8080 8090 8761 8888"`
2. Check Eureka dashboard: http://localhost:8761
3. Restart services with `.\start-all-services.ps1`

## Security Configuration

Each protected service has a `SecurityConfig` class that:
1. Disables CSRF (for REST APIs)
2. Sets session management to STATELESS
3. Permits public endpoints (/actuator, /swagger-ui, etc.)
4. Requires authentication for all other endpoints
5. Uses `JwtAuthenticationFilter` to validate tokens

## Next Steps

To add JWT security to additional services:
1. Add dependencies to `pom.xml`:
   - `spring-boot-starter-security`
   - `io.jsonwebtoken:jjwt-api`, `jjwt-impl`, `jjwt-jackson` (version 0.11.5)
2. Create `JwtUtil` class for token validation
3. Create `JwtAuthenticationFilter` to intercept requests
4. Create `SecurityConfig` to configure Spring Security
5. Add `jwt.secret` to `application.yml`

## Files Created
- `start-all-services.ps1` - Starts all infrastructure services
- `test-jwt-auth.ps1` - Tests JWT authentication flow
- `JWT-AUTHENTICATION-GUIDE.md` - This guide

## Important Notes

- **Never commit JWT secrets to Git** - Use environment variables in production
- **Use HTTPS in production** - JWTs should never be sent over unencrypted connections
- **Implement token refresh** - Don't make users re-login every hour
- **Add role-based access control** - Use `@PreAuthorize` annotations for fine-grained permissions
- **Implement token revocation** - Consider Redis/database for blacklisting tokens
