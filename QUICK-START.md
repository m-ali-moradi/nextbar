# Quick Start Guide

Get the NextBar platform running locally in minutes.

---

## Prerequisites

| Tool      | Version | Required For           |
|-----------|---------|------------------------|
| Java      | 21+     | All backend services   |
| Maven     | 3.8+    | Building services      |
| Node.js   | 22+     | Frontend               |
| Docker    | 24+     | Infrastructure (MySQL, RabbitMQ, Observability) |

---

## Step 0: Configure Environment Variables

Create a local `.env` file from the template and fill in your secrets:

```powershell
# PowerShell
Copy-Item .env.example .env
```

```bash
# Bash / macOS / Linux
cp .env.example .env
```

Open `.env` and set **all** required variables:

| Variable                  | Required | Description                            |
|---------------------------|----------|----------------------------------------|
| `DB_USERNAME`             | Yes      | MySQL username                         |
| `DB_PASSWORD`             | Yes      | MySQL password                         |
| `JWT_SECRET`              | Yes      | Base64-encoded 256-bit key for JWT     |
| `INTERNAL_SERVICE_SECRET` | Yes      | Long random secret for service auth    |
| `RABBITMQ_USERNAME`       | Yes      | RabbitMQ username                      |
| `RABBITMQ_PASSWORD`       | Yes      | RabbitMQ password                      |
| `CONFIG_REPO_GIT_TOKEN`   | Yes      | GitHub PAT for Config Server           |

> See [`.env.example`](.env.example) for the full list of 90+ configurable variables covering all services.

---

## Step 1: Start Infrastructure with Docker Compose

Start MySQL, RabbitMQ, and the observability stack:

```bash
docker compose --env-file .env up -d mysql rabbitmq zipkin loki prometheus grafana
```

This spins up:

| Service        | Port  | Description                     |
|----------------|-------|---------------------------------|
| MySQL          | 3306  | Shared database                 |
| RabbitMQ       | 5672  | Message broker (AMQP)           |
| RabbitMQ UI    | 15672 | Management console              |
| Zipkin         | 9411  | Distributed tracing             |
| Loki           | 3100  | Log aggregation                 |
| Prometheus     | 9090  | Metrics collection              |
| Grafana        | 3000  | Monitoring dashboards           |

Wait until all containers are healthy:
```bash
docker compose ps
```

---

## Step 2: Build All Services

The project uses a **centralized BOM** (`nextbar-bom`). Build everything from the project root:

```bash
mvn clean install -DskipTests
```

This builds all 8 modules in one shot:
- `servers/config-server`
- `servers/eureka-server`
- `servers/gateway`
- `users-service`
- `bar-service`
- `eventPlanner-service`
- `dropPoint-service`
- `warehouse-service`

> **Tip:** To build a single service, run `mvn clean install -DskipTests` inside its directory.

---

## Step 3: Start Spring Services (In Order)

Services **must** be started in the following order because of configuration and discovery dependencies.

### 1. Config Server (Port 8888)
```bash
cd servers/config-server
# Required for private GitHub config repository access
# PowerShell:
$env:SPRING_CLOUD_CONFIG_SERVER_GIT_USERNAME = "<github-username>"
$env:SPRING_CLOUD_CONFIG_SERVER_GIT_TOKEN = "<github-personal-access-token>"

# Bash / macOS / Linux:
export SPRING_CLOUD_CONFIG_SERVER_GIT_USERNAME="<github-username>"
export SPRING_CLOUD_CONFIG_SERVER_GIT_TOKEN="<github-personal-access-token>"

mvn spring-boot:run
```
Wait for: `Started ConfigServerApplication`

### 2. Eureka Server (Port 8761)
```bash
cd servers/eureka-server
mvn spring-boot:run
```
Wait for: `Started EurekaServerApplication`
Verify: Open [http://localhost:8761](http://localhost:8761)

### 3. API Gateway (Port 8080)
```bash
cd servers/gateway
mvn spring-boot:run
```
Wait for: Gateway registers with Eureka

### 4. Business Services (Any Order)

Open a separate terminal for each:

```bash
# Users Service (Port 8090)
cd users-service
mvn spring-boot:run

# Bar Service (Port 8081)
cd bar-service
mvn spring-boot:run

# Event Planner Service (Port 8082)
cd eventPlanner-service
mvn spring-boot:run

# Drop Points Service (Port 8083)
cd dropPoint-service
mvn spring-boot:run

# Warehouse Service (Port 8085)
cd warehouse-service
mvn spring-boot:run
```

Wait for each: Service registers with Eureka

---

## Step 4: Start Frontend

```bash
cd servers/frontend
npm install
npm run dev
```

Frontend available at: [http://localhost:5173](http://localhost:5173)

---

## Step 5: Verify Everything is Running

### Eureka Dashboard

Open [http://localhost:8761](http://localhost:8761). You should see **all 6 services** registered:

| Eureka Name             | Service              |
|-------------------------|----------------------|
| `GATEWAY`               | API Gateway          |
| `USERS-SERVICE`         | Users Service        |
| `BAR-SERVICE`           | Bar Service          |
| `EVENTPLANNER-SERVICE`  | Event Planner        |
| `DROPPOINT-SERVICE`     | Drop Points          |
| `WAREHOUSE-SERVICE`     | Warehouse            |

### Health Checks

```
http://localhost:8888/actuator/health    # Config Server
http://localhost:8090/actuator/health    # Users Service
http://localhost:8081/actuator/health    # Bar Service
http://localhost:8082/actuator/health    # Event Planner
http://localhost:8083/actuator/health    # Drop Points
http://localhost:8085/actuator/health    # Warehouse
```

### Observability Stack

| Tool       | URL                                              |
|------------|--------------------------------------------------|
| Grafana    | [http://localhost:3000](http://localhost:3000)    |
| Prometheus | [http://localhost:9090](http://localhost:9090)    |
| Zipkin     | [http://localhost:9411](http://localhost:9411)    |
| RabbitMQ   | [http://localhost:15672](http://localhost:15672)  |

---

## Step 6: Log In as Default Admin

A default admin user is created on startup:

- **URL:** [http://localhost:5173/login](http://localhost:5173/login)
- **Email/Username:** `admin` (or `admin@nextbar.com`)
- **Password:** `admin123`

This user has the `ADMIN` role mapped to all core services (`BAR_SERVICE`, `EVENT_SERVICE`, `DROP_POINT_SERVICE`, `WAREHOUSE_SERVICE`).

---

## Full Docker Deployment

To run **everything** in Docker (including all Spring services and the frontend), use:

```bash
docker compose --env-file .env up -d
```

This spins up **15 containers**:

| Container | Image | Port(s) |
|-----------|-------|---------|
| `nextbar-mysql` | mysql:8.0.36 | 3306 |
| `nextbar-rabbitmq` | rabbitmq:3.13-management | 5672, 15672 |
| `nextbar-zipkin` | openzipkin/zipkin:3.4 | 9411 |
| `nextbar-loki` | grafana/loki:2.9.6 | 3100 |
| `nextbar-prometheus` | prom/prometheus:v2.51.2 | 9090 |
| `nextbar-grafana` | grafana/grafana:10.4.2 | 3000 |
| `nextbar-config-server` | Built from Dockerfile | 8888 |
| `nextbar-eureka-server` | Built from Dockerfile | 8761 |
| `nextbar-gateway` | Built from Dockerfile | 8080 |
| `nextbar-users-service` | Built from Dockerfile | 8090 |
| `nextbar-bar-service` | Built from Dockerfile | 8081 |
| `nextbar-eventplanner-service` | Built from Dockerfile | 8082 |
| `nextbar-droppoint-service` | Built from Dockerfile | 8083 |
| `nextbar-warehouse-service` | Built from Dockerfile | 8085 |
| `nextbar-frontend` | Built from Dockerfile (Nginx) | 80 |

MySQL databases are auto-created on first start via `init-databases.sql`:
- `bar_db` · `user_db` · `event_db` · `drop_points_db` · `warehouse_db`

All services have health checks with `depends_on` chains ensuring correct startup order. Memory limits are set per container (128M–512M).

> See the [Deployment Plan](docs/DeploymentPlan.md) for full Azure production deployment details.

---

## Testing

### Backend — Java (JUnit 5 + JaCoCo)

```bash
# Run all tests across all modules
mvn test

# Run tests for a specific service
cd bar-service && mvn test

# Generate JaCoCo coverage report
mvn verify
# Reports: <service>/target/site/jacoco/index.html
```

**Test suites across the project:**

| Service | Test Classes |
|---------|-------------|
| Bar | `BarApplicationTest` · `SupplyRequestServiceImplTest` · `UsageLogServiceImplTest` |
| Event Planner | `EventPlannerApplicationTests` · `CreateRequestsValidationTest` · `EventServiceStartEventTest` · `EventServiceCancelEventTest` · `EventServicePublishingTest` |
| Drop Points | `DropPointsSysApplicationTests` · `DropPointServiceTest` |
| Users | `AuthControllerTest` · `LoginAttemptServiceTest` · `TokenLifecycleServiceTest` |
| Warehouse | `StockServiceTest` |

### Frontend — Vue (Vitest + ESLint)

```bash
cd servers/frontend
npm run test:unit    # Vitest
npm run lint         # ESLint
```

### Manual API Testing with Swagger

#### 1. Login (get a JWT token)

Open the Users Service Swagger UI:
[http://localhost:8090/swagger-ui.html](http://localhost:8090/swagger-ui.html)

1. Call `POST /api/v1/users/login` with valid credentials (or register a user first)
2. **Copy the JWT token** from the response

#### 2. Authorize other services

1. Open any other service's Swagger UI (see table below)
2. Click the **Authorize** button
3. Enter: `Bearer YOUR_JWT_TOKEN`
4. Click **Authorize**

#### 3. Service Swagger UIs

| Service        | Swagger URL                                     |
|----------------|------------------------------------------------|
| Users          | http://localhost:8090/swagger-ui.html           |
| Bar            | http://localhost:8081/swagger-ui.html           |
| Event Planner  | http://localhost:8082/swagger-ui.html           |
| Drop Points    | http://localhost:8083/swagger-ui.html           |
| Warehouse      | http://localhost:8085/swagger-ui.html           |

> **Alternative URL:** If `/swagger-ui.html` doesn't load, try `/swagger-ui/index.html`

---

## Troubleshooting

### Service Won't Start

1. **Config Server must be running first** — all services fetch configuration from it
2. **Eureka Server must be running** — services need it for registration
3. **Check if the port is in use:**
   ```powershell
   netstat -ano | findstr :8081
   ```
4. Check the console logs for detailed error messages

### Not Registering with Eureka

1. Verify Config Server is healthy: `http://localhost:8888/actuator/health`
2. Check `eureka.client.service-url.defaultZone` in the service's config-repo YAML
3. Verify network connectivity to Eureka at port 8761

### Swagger UI Not Loading

1. Rebuild the service: `mvn clean install -DskipTests`
2. Verify `springdoc-openapi-starter-webmvc-ui` is in the service's `pom.xml`
3. Try alternative URL: `/swagger-ui/index.html`

### 401 Unauthorized

1. Login via Users Service first (`POST /api/v1/users/login`)
2. Copy the JWT token from the response
3. Use the **Authorize** button in Swagger
4. Format: `Bearer YOUR_TOKEN`

### Docker Compose Issues

1. Ensure `.env` file exists with all required variables
2. Check container logs: `docker compose logs <service-name>`
3. Restart unhealthy containers: `docker compose restart <service-name>`

### RabbitMQ Connection Refused

1. Ensure RabbitMQ container is running: `docker compose ps rabbitmq`
2. Verify credentials in `.env` match what RabbitMQ was initialized with
3. If credentials changed, you may need to delete the volume:
   ```bash
   docker compose down -v
   docker compose --env-file .env up -d rabbitmq
   ```

---

## Success Indicators

You'll know everything is working when:

- All 6 microservices appear in the [Eureka dashboard](http://localhost:8761)
- Gateway shows as registered
- All Swagger UIs load correctly
- You can login and get a JWT token
- Authenticated requests work across services
- Frontend loads at [http://localhost:5173](http://localhost:5173)
- Observability tools are accessible (Grafana, Prometheus, Zipkin)

---
