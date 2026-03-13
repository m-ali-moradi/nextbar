# Implementation

## Overview

This document describes the **microservices implementation patterns** used in NextBar, focusing on the architectural patterns, integration strategies, and deployment infrastructure that underpin the system.

---

## Microservice Implementation Patterns

### Database-per-Service

Each bounded context owns an isolated MySQL database, provisioned automatically via `init-databases.sql`:

```sql
CREATE DATABASE IF NOT EXISTS user_db;
CREATE DATABASE IF NOT EXISTS bar_db;
CREATE DATABASE IF NOT EXISTS event_db;
CREATE DATABASE IF NOT EXISTS drop_points_db;
CREATE DATABASE IF NOT EXISTS warehouse_db;
```

JPA (Hibernate) manages schema generation with `spring.jpa.hibernate.ddl-auto=update`. Each service connects exclusively to its own database — there are no cross-database joins, shared tables, or distributed transactions.

### Event-Driven Communication (Spring Cloud Stream)

All asynchronous inter-service communication is implemented using **Spring Cloud Stream** with **RabbitMQ** bindings. This provides:

- **Declarative binding configuration** — Consumers and producers defined in YAML (`spring.cloud.stream.bindings`)
- **Automatic serialization** — JSON payloads via message converters
- **Dead Letter Queues** — Failed messages routed to DLQ for inspection
- **Retry with back-off** — Configurable retry attempts before DLQ routing
- **Exchange types** — Topic exchanges for targeted routing, fanout for broadcast (e.g., `event.completed`)

Example producer (Bar Service):
```java
@Autowired
private StreamBridge streamBridge;

public void publishSupplyRequest(SupplyRequestDto request) {
    streamBridge.send("supply.request.created", request);
}
```

Example consumer (Warehouse Service):
```java
@Bean
public Consumer<SupplyRequestCreatedEvent> supplyRequestCreated() {
    return event -> supplyRequestService.processIncoming(event);
}
```

### Synchronous Communication (OpenFeign + Resilience4j)

The Event Planner Service is the **only service** that makes synchronous inter-service calls, using OpenFeign declarative REST clients:

```java
@FeignClient(name = "users-service", fallback = UserServiceClientFallback.class)
public interface UserServiceClient {
    @GetMapping("/api/v1/users/{id}")
    UserDto getUser(@PathVariable UUID id);
}
```

Each Feign client is protected by a **Resilience4j circuit breaker**:

| Parameter | Value |
|-----------|-------|
| Sliding window size | 10 calls |
| Failure rate threshold | 50% |
| Wait duration in open state | 10 seconds |
| Permitted calls in half-open | 3 |
| Fallback class | Returns safe defaults (empty lists, null objects) |

### Anti-Corruption Layer Pattern

When bounded contexts have different models for the same concept, an **Anti-Corruption Layer** translates between them:

1. **Warehouse → Bar (Supply Request Updates):** The Warehouse uses `SupplyRequestStatus` (4 values). The Bar Service maps this to its own `SupplyStatus` enum (5 values, includes COMPLETED) in the event listener.

2. **Warehouse → Drop Point (Collection Lifecycle):** The Warehouse publishes `CollectionStatus` updates. The Drop Point Service translates these into its own `DropPointStatus` state machine transitions.

3. **Event Planner → Bar/Drop Point (Bootstrap):** Planning-phase models (`Event.Bar`, `Event.DropPoint`) are projected into runtime models (`bar.Bar`, `dropPoint.DropPoint`) with different schemas, fields, and lifecycles.

---

## Service Architecture

### Layered Architecture per Service

Each microservice follows a consistent layered architecture:

```
┌─────────────────────────────────────┐
│  Controller Layer                   │  REST endpoints, request validation
├─────────────────────────────────────┤
│  Service Layer                      │  Business logic, domain operations
├─────────────────────────────────────┤
│  Event Layer                        │  RabbitMQ publishers and consumers
├─────────────────────────────────────┤
│  Repository Layer                   │  JPA/Spring Data access
├─────────────────────────────────────┤
│  Model Layer                        │  JPA entities, enums, value objects
├─────────────────────────────────────┤
│  DTO Layer                          │  Request/Response data transfer objects
├─────────────────────────────────────┤
│  Mapper Layer                       │  Entity-to-DTO conversions
├─────────────────────────────────────┤
│  Security Layer                     │  JWT filter, RBAC service, internal auth
└─────────────────────────────────────┘
```

### Data Bootstrapping

Several mechanisms are employed to inject default startup configurations:

1. **Users Service:** `DataBootstrap.java` implements a `CommandLineRunner` and runs on startup to check for standard service definitions and the generic `admin` user. It will provision `admin` / `admin123` attached to all domains if not yet created.
2. **Databases:** `init-databases.sql` initializes default schemas for active containers so the stack boots seamlessly.

### Security Implementation

Every business service implements a consistent security stack:

| Component | Class | Purpose |
|-----------|-------|---------|
| JWT Filter | `JwtAuthenticationFilter` | Extracts and validates JWT from `Authorization` header |
| RBAC Service | `RbacService` | Evaluates role claims (`SERVICE:ROLE:RESOURCE_ID`) against request context |
| Internal Auth | `InternalRequestVerificationFilter` | Validates shared secret for service-to-service REST calls |

The Gateway implements additional security:
- `JwtAuthenticationGatewayFilterFactory` — Route-level JWT validation
- `LoginRateLimiter` — In-memory rate limiting (10 req/60s, 300s block)
- `SecurityHeadersWebFilter` — CSP, HSTS, Referrer-Policy, Permissions-Policy
- `TokenStatusClient` — Calls Users Service to check token blacklist status

---

## Infrastructure Services

### Config Server (Port 8888)

Spring Cloud Config Server backed by a **Git repository** (default profile: `git`). On startup, the server clones the NextBar GitHub repository and serves YAML configurations from the `config-repo/` path. A `native` profile fallback is available for local development using `file:../../config-repo`. All services fetch configuration with `failfast=true` and retry.

**Git source:** `https://github.com/m-ali-moradi/nextBar` (branch: `main`, search-path: `config-repo`, clone-on-start: `true`, force-pull: `true`)

**Managed config files:** `bar-service.yml`, `droppoint-service.yml`, `eventplanner-service.yml`, `warehouse-service.yml`, `users-service.yml`, `gateway.yml`, `prometheus.yml`

### Eureka Server (Port 8761)

Netflix Eureka service registry. All services register on startup, enabling the Gateway to use `lb://` load-balanced routing. Health checks ensure unhealthy instances are automatically deregistered.

### API Gateway (Port 8080)

Spring Cloud Gateway (WebFlux) acting as the single entry point for all client traffic:

| Concern | Implementation |
|---------|---------------|
| **Routing** | Dynamic route definitions via Eureka `lb://` URIs |
| **Authentication** | `JwtAuthenticationGatewayFilterFactory` per route |
| **Rate Limiting** | `LoginRateLimiter` for brute-force protection |
| **Security Headers** | `SecurityHeadersWebFilter` (CSP, HSTS, etc.) |
| **WebSocket** | `EventWebSocketHandler` relays RabbitMQ → browser via `RabbitEventListener` |
| **CORS** | Configured per environment |

---

## Containerization & Deployment

### Docker Multi-Stage Builds

All services use multi-stage Docker builds with Spring Boot layered JARs:

```dockerfile
FROM eclipse-temurin:21-jre-alpine
# Non-root user for security
RUN addgroup -S app && adduser -S app -G app
# Layered JAR extraction for caching
COPY --from=builder /app/dependencies/ ./
COPY --from=builder /app/spring-boot-loader/ ./
COPY --from=builder /app/snapshot-dependencies/ ./
COPY --from=builder /app/application/ ./
USER app
```

### Docker Compose Stack (15 Containers)

The full platform runs as 15 containers with health-based startup ordering:

| Layer | Containers | Startup Order |
|-------|-----------|---------------|
| **Data** | MySQL, RabbitMQ | 1st (no dependencies) |
| **Infrastructure** | Config Server, Eureka | 2nd (depends on data) |
| **Observability** | Zipkin, Prometheus, Loki, Grafana | 2nd (parallel with infra) |
| **Business** | Users, Bar, Event Planner, Drop Points, Warehouse | 3rd (depends on infra) |
| **Edge** | Gateway, Frontend (Nginx) | 4th (depends on business services) |

### CI/CD Pipeline (GitHub Actions)

| Stage | Trigger | Actions |
|-------|---------|---------|
| **Test** | Push / PR to `main` | `mvn verify` (all modules) + frontend lint & build |
| **Build & Push** | Push to `main` | Docker build → Trivy scan (CRITICAL/HIGH) → Azure Container Registry |
| **Deploy Staging** | After build | Automated to Azure Container Apps |
| **Deploy Production** | After staging | Manual approval via GitHub Environment protection rules |

### Security Scanning

| Tool | Scope | Threshold |
|------|-------|-----------|
| **CodeQL** | Java + JavaScript/TypeScript SAST | Automated alerts |
| **Trivy** | Container image scanning | Fails on CRITICAL/HIGH |
| **OWASP Dependency-Check** | Java/npm dependency audit | Fails on CVSS >= 7 |
| **npm audit** | Frontend npm packages | Advisory-level reporting |

---

