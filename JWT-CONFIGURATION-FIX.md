# JWT Configuration Fix - Complete Solution

## Summary of Issues Fixed

### 1. **JWT Secret Mismatch**
- **Problem**: Different JWT secrets across services
  - User Service & Gateway: `yourSecretKeyForJWTTokenGenerationMustBeLongEnoughForHS256Algorithm`
  - Bar Service: `2YrmlATq8aGvu9r5OSwV2s3g6tZqYbhyZnMNsnU/wvO27lfYxbgm5foq7F2Vsmw1eZC4WbdKHx3IXW5J7PddEA==`
- **Solution**: All services now use the same strong JWT secret

### 2. **Gateway Security Blocking Requests**
- **Problem**: Gateway's `SecurityWebFilterChain` was requiring authentication for all requests BEFORE the JWT filter could run
- **Solution**: Changed to `.anyExchange().permitAll()` - JWT filter now handles authentication

### 3. **Missing Roles in JWT Token**
- **Problem**: JWT tokens didn't include user roles, causing authorization issues
- **Solution**: Updated `JwtUtil` and `UserService` to include roles in JWT claims

### 4. **Invalid Gateway Filter Configuration**
- **Problem**: `JwtAuthenticationFilter` referenced in gateway.yml but Spring Cloud Gateway requires a `GatewayFilterFactory`
- **Solution**: Created `JwtAuthenticationGatewayFilterFactory` for proper integration

## Files Modified

### API Gateway
1. **GatewaySecurityConfig.java** - Removed premature authentication check
2. **JwtAuthenticationGatewayFilterFactory.java** - NEW FILE - Proper filter factory for Spring Cloud Gateway
3. **gateway.yml** (config-repo) - Added JWT secret and updated filter reference

### Users Service
1. **JwtUtil.java** - Added method to include roles in JWT token
2. **UserService.java** - Updated login/register to pass roles to JWT
3. **users-service.yml** (config-repo) - Added shared JWT secret

### Bar Service
1. **bar-service.yml** (config-repo) - Added shared JWT secret

## Configuration Files (config-repo)

### gateway.yml
```yaml
security:
  jwt:
    secret: 2YrmlATq8aGvu9r5OSwV2s3g6tZqYbhyZnMNsnU/wvO27lfYxbgm5foq7F2Vsmw1eZC4WbdKHx3IXW5J7PddEA==
    expiration: 3600000

routes:
  - id: bar-service
    uri: lb://bar-service
    predicates:
      - Path=/api/bars/**
    filters:
      - StripPrefix=1
      - JwtAuthenticationGatewayFilterFactory
```

### users-service.yml
```yaml
security:
  jwt:
    secret: 2YrmlATq8aGvu9r5OSwV2s3g6tZqYbhyZnMNsnU/wvO27lfYxbgm5foq7F2Vsmw1eZC4WbdKHx3IXW5J7PddEA==
    expiration: 3600000
```

### bar-service.yml
```yaml
jwt:
  secret: 2YrmlATq8aGvu9r5OSwV2s3g6tZqYbhyZnMNsnU/wvO27lfYxbgm5foq7F2Vsmw1eZC4WbdKHx3IXW5J7PddEA==
```

## Testing Instructions

### Step 1: Update Config Repository
Since your config files are in GitHub, you need to:
1. Commit and push the changes in `config-repo/` folder to GitHub
2. Restart config-server to pick up new configurations

### Step 2: Restart Services in Order
```bash
# 1. Config Server (port 8888)
cd servers/config-server
mvn spring-boot:run

# 2. Eureka Server (port 8761)
cd servers/eureka-server
mvn spring-boot:run

# 3. Users Service (port 8090)
cd users-service
mvn spring-boot:run

# 4. Bar Service (port 8081)
cd bar-service
mvn spring-boot:run

# 5. API Gateway (port 8080)
cd servers/gateway
mvn spring-boot:run

# 6. Frontend (port 5174)
cd servers/frontend
npm run dev
```

### Step 3: Test Login Flow

#### 3.1 Test Login via API Gateway
```bash
# Login request
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# Expected response:
# {
#   "token": "eyJhbGciOiJIUzI1NiJ9...",
#   "username": "testuser",
#   "email": "test@example.com",
#   "userId": 1
# }
```

#### 3.2 Test Bar Data Access
```bash
# Get all bars (use token from login)
curl -X GET http://localhost:8080/api/bars \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"

# Expected response: List of bars
```

### Step 4: Test from Frontend

1. Open browser: `http://localhost:5174`
2. Login with credentials
3. Navigate to bars page
4. Verify bar data loads successfully

### Step 5: Verify JWT Token

You can decode the JWT token at https://jwt.io to verify it contains:
- `sub` (subject): username
- `roles`: ["USER"]
- `iat` (issued at): timestamp
- `exp` (expiration): timestamp

## Request Flow Diagram

```
Frontend (5174)
    ↓
    | POST /api/auth/login
    ↓
API Gateway (8080)
    ↓
    | (No JWT check for /api/auth/**)
    ↓
Users Service (8090)
    ↓
    | Returns JWT token with roles
    ↓
Frontend stores token
    ↓
    | GET /api/bars with Authorization header
    ↓
API Gateway (8080)
    ↓
    | JwtAuthenticationGatewayFilterFactory validates token
    | Extracts user info and roles
    | Adds X-User-Id and X-Roles headers
    ↓
Bar Service (8081)
    ↓
    | JwtAuthenticationFilter validates token again
    | Sets SecurityContext
    ↓
Returns bar data
```

## Common Issues & Solutions

### Issue 1: 401 Unauthorized on Login
**Symptom**: Cannot login, getting 401 error  
**Solution**: 
- Check users service is running on port 8090
- Verify user exists in database
- Check gateway can reach users-service via Eureka

### Issue 2: 401 Unauthorized on Bar Data
**Symptom**: Login works but cannot fetch bar data  
**Solution**:
- Verify JWT token is being sent in Authorization header
- Check all services have same JWT secret
- Restart services after config changes

### Issue 3: Config Changes Not Applied
**Symptom**: Services still using old configuration  
**Solution**:
- Push config changes to GitHub
- Restart config-server first
- Then restart all other services
- Use `/actuator/refresh` endpoint to reload config without restart:
  ```bash
  curl -X POST http://localhost:8080/actuator/refresh
  ```

### Issue 4: Gateway Cannot Find Services
**Symptom**: "503 Service Unavailable" from gateway  
**Solution**:
- Check Eureka dashboard: http://localhost:8761
- Verify all services are registered
- Wait 30 seconds after starting services for registration

## Environment Variables (Optional)

For production, use environment variables instead of hardcoded secrets:

```bash
# Set same secret for all services
export JWT_SECRET="2YrmlATq8aGvu9r5OSwV2s3g6tZqYbhyZnMNsnU/wvO27lfYxbgm5foq7F2Vsmw1eZC4WbdKHx3IXW5J7PddEA=="

# In application.yml
jwt:
  secret: ${JWT_SECRET}
```

## Security Recommendations

1. **Change JWT Secret**: The current secret is shown in this document. Generate a new one:
   ```bash
   # Generate new 512-bit secret
   openssl rand -base64 64
   ```

2. **Enable HTTPS**: In production, use HTTPS for all communications

3. **Implement Role-Based Access Control**: Extend the role system to have different roles (ADMIN, BAR_MANAGER, USER)

4. **Add Token Refresh**: Implement refresh token mechanism for better security

5. **Add Rate Limiting**: Protect against brute force attacks on login endpoint

## Additional Enhancements Needed

1. **Role Management**: Currently all users get "USER" role. Implement proper role assignment
2. **Token Refresh**: Add refresh token endpoint
3. **Logout**: Implement token blacklist for logout
4. **User Registration**: Add proper user registration flow
5. **Error Handling**: Improve error messages for authentication failures
