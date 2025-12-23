# 🚀 Quick Start Guide - After Configuration Fixes

## Prerequisites
- Java 21 installed
- Maven installed
- MySQL running (for users-service)

---

## Step 1: Rebuild All Services

Run this command in the project root to rebuild all services:

```bash
# Navigate to each service and rebuild
cd bar-service && mvn clean install && cd ..
cd event-planner-service && mvn clean install && cd ..
cd drop-points-service && mvn clean install && cd ..
cd warehouse-service && mvn clean install && cd ..
cd users-service && mvn clean install && cd ..
cd servers/config-server && mvn clean install && cd ../..
cd servers/eureka-server && mvn clean install && cd ../..
cd servers/gateway && mvn clean install && cd ../..
```

Or rebuild individually as needed.

---

## Step 2: Start Services (Correct Order)

### **1. Config Server** (Port 8888)
```bash
cd servers/config-server
mvn spring-boot:run
```
✅ Wait for: "Started ConfigServerApplication"

### **2. Eureka Server** (Port 8761)
```bash
cd servers/eureka-server
mvn spring-boot:run
```
✅ Wait for: "Started EurekaServerApplication"
✅ Verify: http://localhost:8761

### **3. Gateway** (Port 8080)
```bash
cd servers/gateway
mvn spring-boot:run
```
✅ Wait for: Gateway registers with Eureka

### **4. Users Service** (Port 8090)
```bash
cd users-service
mvn spring-boot:run
```
✅ Wait for: Service registers with Eureka

### **5. Bar Service** (Port 8081)
```bash
cd bar-service
mvn spring-boot:run
```
✅ Wait for: Service registers with Eureka

### **6. Event Planner Service** (Port 8082)
```bash
cd event-planner-service
mvn spring-boot:run
```
✅ Wait for: Service registers with Eureka

### **7. Drop Points Service** (Port 8083)
```bash
cd drop-points-service
mvn spring-boot:run
```
✅ Wait for: Service registers with Eureka

### **8. Warehouse Service** (Port 8085)
```bash
cd warehouse-service
mvn spring-boot:run
```
✅ Wait for: Service registers with Eureka

---

## Step 3: Verify Everything is Running

### Check Eureka Dashboard
Open: http://localhost:8761

You should see all services registered:
- GATEWAY
- USERS-SERVICE
- BAR-SERVICE
- EVENTPLANNER-SERVICE
- DROPPOINT-SERVICE
- WAREHOUSE-SERVICE

---

## Step 4: Test with Swagger

### 1. Users Service - Login
1. Open: http://localhost:8090/swagger-ui.html
2. POST /auth/login
3. Use credentials (or register first)
4. **Copy the JWT token**

### 2. Authorize Other Services
1. Open any service Swagger: http://localhost:8081/swagger-ui.html
2. Click **"Authorize"** button
3. Enter: `Bearer YOUR_JWT_TOKEN`
4. Click Authorize

### 3. Test Endpoints
Now you can test all endpoints in:
- **Bar Service**: http://localhost:8081/swagger-ui.html
- **Event Planner**: http://localhost:8082/swagger-ui.html
- **Drop Points**: http://localhost:8083/swagger-ui.html
- **Warehouse**: http://localhost:8085/swagger-ui.html

---

## Step 5: Start Frontend (Optional)

```bash
cd servers/frontend
npm install
npm run dev
```

Frontend will be available at: http://localhost:5174

---

## 🎯 Quick Health Checks

### All Services Running?
```bash
# Eureka Dashboard
http://localhost:8761

# Config Server
http://localhost:8888/actuator/health

# All Service Health
http://localhost:8090/actuator/health  # Users
http://localhost:8081/actuator/health  # Bar
http://localhost:8082/actuator/health  # Events
http://localhost:8083/actuator/health  # Drop Points
http://localhost:8085/actuator/health  # Warehouse
```

### All Swagger UIs Accessible?
- http://localhost:8090/swagger-ui.html ✓
- http://localhost:8081/swagger-ui.html ✓
- http://localhost:8082/swagger-ui.html ✓
- http://localhost:8083/swagger-ui.html ✓
- http://localhost:8085/swagger-ui.html ✓

---

## 📝 What Changed?

### ✅ **Added Bootstrap.yml**
- bar-service/src/main/resources/bootstrap.yml
- event-planner-service/src/main/resources/bootstrap.yml
- drop-points-service/src/main/resources/bootstrap.yml
- warehouse-service/src/main/resources/bootstrap.yml

### ✅ **Added Dependencies**
- Spring Cloud Config Client (bar-service, drop-points-service)
- Spring Cloud Bootstrap (all services)
- Swagger/OpenAPI (users-service)

### ✅ **Now All Services**
- ✅ Connect to Config Server
- ✅ Register with Eureka
- ✅ Have Swagger UI enabled
- ✅ Support JWT authentication

---

## 🐛 Troubleshooting

### Service Won't Start
1. Check Config Server is running first
2. Check Eureka Server is running
3. Check port is not already in use
4. Check logs for detailed errors

### Not Registering with Eureka
1. Verify `@EnableDiscoveryClient` annotation
2. Check eureka.client.service-url.defaultZone in config
3. Verify network connectivity to Eureka

### Swagger UI Not Loading
1. Rebuild the service: `mvn clean install`
2. Check dependency is in pom.xml
3. Try alternative URL: `/swagger-ui/index.html`

### 401 Unauthorized
1. Login via Users Service first
2. Copy JWT token
3. Use "Authorize" button in Swagger
4. Format: `Bearer YOUR_TOKEN`

---

## 🎉 Success Indicators

You'll know everything is working when:

✅ All 6 microservices appear in Eureka dashboard
✅ Gateway shows as registered
✅ All Swagger UIs load correctly
✅ You can login and get a JWT token
✅ Authenticated requests work in Swagger
✅ Frontend can communicate with backend

---

**You're all set! Happy coding! 🚀**
