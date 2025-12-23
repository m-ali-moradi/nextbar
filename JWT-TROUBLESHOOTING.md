# JWT Authentication - Quick Troubleshooting Checklist

## Before Starting Services

- [ ] Config repository changes committed and pushed to GitHub
- [ ] All services have been rebuilt (if Java code changed)
- [ ] No services are already running on required ports

## Service Startup Order

1. [ ] Config Server (8888) - Wait until "Started ConfigServerApplication"
2. [ ] Eureka Server (8761) - Wait until dashboard accessible
3. [ ] Users Service (8090) - Check it registers with Eureka
4. [ ] Bar Service (8081) - Check it registers with Eureka  
5. [ ] Gateway (8080) - Check it registers with Eureka
6. [ ] Frontend (5174)

## Verification Steps

### Check Config Server
```bash
# Verify config server is serving gateway config
curl http://localhost:8888/gateway/default

# Should see JWT secret in response
```

### Check Eureka Registration
1. Open http://localhost:8761
2. Verify you see:
   - USERS-SERVICE
   - BAR-SERVICE
   - GATEWAY

### Test Direct Service Access (Without Gateway)

#### Users Service Direct:
```bash
# Should work - no auth required on this endpoint
curl -X POST http://localhost:8090/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "your-username", "password": "your-password"}'
```

#### Bar Service Direct:
```bash
# Should return 401 - requires JWT
curl http://localhost:8081/bars
```

### Test Through Gateway

#### Login Through Gateway:
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username": "your-username", "password": "your-password"}'

# Save the token from response
```

#### Access Bar Service Through Gateway:
```bash
# Replace TOKEN with actual token from login
curl http://localhost:8080/api/bars \
  -H "Authorization: Bearer TOKEN"
```

## Common Error Patterns

### Error: "Whitelabel Error Page" on Gateway
**Cause**: Gateway cannot reach the downstream service  
**Fix**: 
- Check service is registered in Eureka
- Wait 30 seconds after service startup
- Check service name matches in gateway.yml (lb://users-service, lb://bar-service)

### Error: 401 on Login Request
**Cause**: Gateway security is blocking request  
**Fix**: 
- Verify GatewaySecurityConfig allows all exchanges
- Check gateway.yml has correct route for /api/auth/**
- Ensure route doesn't have JWT filter

### Error: 401 After Successful Login
**Cause**: JWT secret mismatch or token not being sent  
**Fix**:
- Verify all three config files (gateway.yml, users-service.yml, bar-service.yml) have same JWT secret
- Check Authorization header format: "Bearer <token>"
- Decode token at jwt.io to verify it's valid

### Error: "Connection refused" or "No instances available"
**Cause**: Service not running or not registered  
**Fix**:
- Check service is running: `netstat -an | findstr "8081"` (Windows) or `lsof -i :8081` (Mac/Linux)
- Verify in Eureka dashboard
- Check eureka.client.serviceUrl.defaultZone in application.yml

## Port Check Commands

### Windows:
```powershell
netstat -an | findstr "8888 8761 8080 8081 8090 5174"
```

### Mac/Linux:
```bash
lsof -i :8888 -i :8761 -i :8080 -i :8081 -i :8090 -i :5174
```

## Quick Reset Procedure

If things are completely broken:

1. **Stop All Services** (Ctrl+C in each terminal)

2. **Clean Build All Services**:
   ```bash
   # For each Spring Boot service
   mvn clean package -DskipTests
   ```

3. **Verify Config Repository**:
   - Check GitHub has latest config changes
   - Verify no merge conflicts

4. **Restart in Order** (wait for each to fully start):
   ```bash
   # Config Server
   cd servers/config-server && mvn spring-boot:run
   
   # Wait for "Started" message, then Eureka
   cd servers/eureka-server && mvn spring-boot:run
   
   # Wait for Eureka UI, then Users Service
   cd users-service && mvn spring-boot:run
   
   # Wait for registration, then Bar Service
   cd bar-service && mvn spring-boot:run
   
   # Wait for registration, then Gateway
   cd servers/gateway && mvn spring-boot:run
   
   # Finally Frontend
   cd servers/frontend && npm run dev
   ```

## Log Locations to Check

### Gateway Logs:
Look for:
- "JWT authentication successful" - Good!
- "JWT authentication failed" - Token validation issue
- "No instances available" - Service discovery issue

### Bar Service Logs:
Look for:
- "JWT authentication successful for user: username" - Good!
- JWT validation errors - Secret mismatch
- "Started BarServiceApplication" - Service started

### Users Service Logs:
Look for:
- "Login successful for user:" - Login endpoint hit
- SQL errors - Database connection issue
- "Started UsersServiceApplication" - Service started

## JWT Token Debugging

Decode your JWT token at https://jwt.io

**Valid Token Should Have**:
```json
{
  "sub": "username",
  "roles": ["USER"],
  "iat": 1234567890,
  "exp": 1234571490
}
```

**Header Should Be**:
```json
{
  "alg": "HS256",
  "typ": "JWT"
}
```

## Final Checklist Before Seeking Help

- [ ] All services show "Started" in their logs
- [ ] All services appear in Eureka dashboard
- [ ] Can access Eureka UI at http://localhost:8761
- [ ] Config server returns config at http://localhost:8888/gateway/default
- [ ] All three config files have identical JWT secret
- [ ] Frontend is running on port 5174
- [ ] Browser console shows no CORS errors
- [ ] JWT secret is at least 256 bits (43+ base64 characters)
- [ ] Authorization header format is: `Bearer <token>` (with space after Bearer)

## Still Not Working?

Check these specific files have correct content:

1. [gateway/src/.../GatewaySecurityConfig.java](servers/gateway/src/main/java/com/conditects/gateway/config/GatewaySecurityConfig.java) - Should have `.anyExchange().permitAll()`

2. [gateway/src/.../JwtAuthenticationGatewayFilterFactory.java](servers/gateway/src/main/java/com/conditects/gateway/filter/JwtAuthenticationGatewayFilterFactory.java) - Should exist

3. [config-repo/gateway.yml](config-repo/gateway.yml) - Should have `security.jwt.secret` property

4. [config-repo/users-service.yml](config-repo/users-service.yml) - Should have `security.jwt.secret` property

5. [config-repo/bar-service.yml](config-repo/bar-service.yml) - Should have `jwt.secret` property

6. [users-service/.../JwtUtil.java](users-service/src/main/java/com/coditects/usersservice/util/JwtUtil.java) - Should have `generateToken(String, List<String>)` method

7. [users-service/.../UserService.java](users-service/src/main/java/com/coditects/usersservice/service/UserService.java) - Should pass roles to `generateToken`
