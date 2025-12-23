# 🔍 Swagger API Testing Guide

## Quick Access URLs

### **Microservices Swagger UI**

| Service | Swagger UI URL | Alternative URL |
|---------|---------------|-----------------|
| **Users Service** | http://localhost:8090/swagger-ui.html | http://localhost:8090/swagger-ui/index.html |
| **Bar Service** | http://localhost:8081/swagger-ui.html | http://localhost:8081/swagger-ui/index.html |
| **Event Planner** | http://localhost:8082/swagger-ui.html | http://localhost:8082/swagger-ui/index.html |
| **Drop Points** | http://localhost:8083/swagger-ui.html | http://localhost:8083/swagger-ui/index.html |
| **Warehouse** | http://localhost:8085/swagger-ui.html | http://localhost:8085/swagger-ui/index.html |

### **OpenAPI JSON Specs**

| Service | OpenAPI Spec URL |
|---------|------------------|
| **Users Service** | http://localhost:8090/v3/api-docs |
| **Bar Service** | http://localhost:8081/v3/api-docs |
| **Event Planner** | http://localhost:8082/v3/api-docs |
| **Drop Points** | http://localhost:8083/v3/api-docs |
| **Warehouse** | http://localhost:8085/v3/api-docs |

---

## 🧪 Testing Workflow

### 1. **Start All Services**

First, ensure all services are running:

```bash
# Check Eureka Dashboard
http://localhost:8761
```

All services should be visible in the Eureka dashboard.

### 2. **Authentication (Users Service)**

#### **Register a New User**
1. Open: http://localhost:8090/swagger-ui.html
2. Navigate to: `auth-controller` → `POST /auth/register`
3. Click "Try it out"
4. Use this sample payload:
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com"
}
```
5. Click "Execute"

#### **Login**
1. Navigate to: `auth-controller` → `POST /auth/login`
2. Click "Try it out"
3. Use credentials:
```json
{
  "username": "testuser",
  "password": "password123"
}
```
4. Click "Execute"
5. **Copy the JWT token** from the response

#### **Authorize Swagger**
1. Click the **"Authorize"** button at the top of Swagger UI
2. Paste the JWT token in the format: `Bearer YOUR_TOKEN_HERE`
3. Click "Authorize" and "Close"

Now all authenticated endpoints will work!

---

### 3. **Test Each Service**

#### **Bar Service** (http://localhost:8081/swagger-ui.html)
Test endpoints like:
- `GET /bars` - Get all bars
- `POST /bars` - Create a new bar
- `GET /bars/{id}` - Get bar by ID
- `PUT /bars/{id}` - Update bar
- `DELETE /bars/{id}` - Delete bar

#### **Event Planner Service** (http://localhost:8082/swagger-ui.html)
Test endpoints like:
- `GET /events` - Get all events
- `POST /events` - Create event
- `GET /events/{id}` - Get event by ID

#### **Drop Points Service** (http://localhost:8083/swagger-ui.html)
Test endpoints like:
- `GET /droppoints` - Get all drop points
- `POST /droppoints` - Create drop point
- `GET /droppoints/{id}` - Get drop point by ID

#### **Warehouse Service** (http://localhost:8085/swagger-ui.html)
Test endpoints like:
- `GET /warehouse` - Get warehouse items
- `POST /warehouse` - Create warehouse item
- `GET /warehouse/{id}` - Get item by ID

---

## 🔐 **Authentication Notes**

### **Services with JWT Authentication**
The following services require JWT tokens:
- ✅ Users Service (for protected endpoints)
- ✅ Bar Service
- ✅ Event Planner Service
- ✅ Warehouse Service

### **How to Use JWT in Swagger**

1. **Get Token**: Login via Users Service (`POST /auth/login`)
2. **Copy Token**: From the response body
3. **Authorize**: Click "Authorize" button in any Swagger UI
4. **Format**: Enter as `Bearer eyJhbGc...` (include "Bearer " prefix)
5. **Test**: All authenticated endpoints will now work

---

## 🌐 **Gateway Access**

All services are also accessible through the API Gateway:

| Service | Direct URL | Via Gateway |
|---------|-----------|-------------|
| Users | http://localhost:8090 | http://localhost:8080/users-service |
| Bar | http://localhost:8081 | http://localhost:8080/bar-service |
| Events | http://localhost:8082 | http://localhost:8080/eventplanner-service |
| Drop Points | http://localhost:8083 | http://localhost:8080/droppoint-service |
| Warehouse | http://localhost:8085 | http://localhost:8080/warehouse-service |

**Note**: When using the gateway, Swagger UI should still be accessed via direct service URLs for proper functionality.

---

## 📊 **Health Checks**

Each service has actuator endpoints:

```bash
# Health check
http://localhost:8090/actuator/health
http://localhost:8081/actuator/health
http://localhost:8082/actuator/health
http://localhost:8083/actuator/health
http://localhost:8085/actuator/health

# Metrics (if exposed)
http://localhost:PORT/actuator/metrics
```

---

## 🐛 **Troubleshooting**

### **Swagger UI Not Loading**
- Verify service is running: Check Eureka dashboard
- Check logs for startup errors
- Verify dependency is in pom.xml: `springdoc-openapi-starter-webmvc-ui`

### **401 Unauthorized Errors**
- Get fresh JWT token from login endpoint
- Ensure "Bearer " prefix in authorization
- Check token expiration

### **404 Not Found**
- Try alternative URL: `/swagger-ui/index.html`
- Verify service is registered in Eureka
- Check service port is correct

### **Connection Refused**
- Start Config Server first
- Start Eureka Server second
- Then start microservices

---

## 💡 **Tips**

1. **Keep Multiple Tabs Open**: Have Swagger UI open in different tabs for each service
2. **Use Eureka Dashboard**: Monitor service health at http://localhost:8761
3. **Check Logs**: Watch console output for errors
4. **Token Management**: Refresh JWT token if testing for extended periods
5. **Postman Alternative**: Export OpenAPI spec from `/v3/api-docs` to use with Postman

---

**Happy Testing! 🚀**
