# Comprehensive Microservice Architecture Improvement Plan
*Project: Bottle Drop Platform - Enterprise-Grade Microservice Showcase*

---

## Executive Summary

This plan transforms the current partially-implemented microservice architecture into a **production-ready, enterprise-grade system** that demonstrates mastery of modern distributed system patterns and technologies. The improvements focus on:

1. **Asynchronous Event-Driven Communication** (Message Broker integration)
2. **Enhanced Observability & Resilience**
3. **Security Hardening**
4. **Data Consistency & Transaction Management**
5. **DevOps & CI/CD Pipeline**
6. **Documentation & Testing**

**Target Audience**: Hiring managers and technical interviewers looking for candidates with real-world microservice expertise.

---

## 🎯 Current State Assessment

### ✅ What's Already Good
- **Core Infrastructure**: Eureka (Service Discovery), Config Server, API Gateway
- **Monitoring Stack**: Prometheus, Grafana, Loki for logging
- **Caching**: Redis implementation
- **Authentication**: JWT-based auth framework
- **Frontend**: Modern Vue 3 + Vite applications
- **Service Isolation**: Each microservice has its own database
- **Technology Stack**: Spring Boot 3.x with Java 21

### ❌ Critical Gaps (from currentProblems.md)
- **Synchronous-only communication**: No message broker for async/event-driven patterns
- **Tight coupling**: Services use OpenFeign for direct REST calls
- **No distributed transactions**: Data inconsistency across service boundaries
- **Incomplete security**: Uneven JWT implementation, no service-to-service auth
- **Limited resilience**: No circuit breakers, retries, or fallbacks
- **Observability gaps**: No distributed tracing or correlation IDs
- **Testing debt**: Missing integration and contract tests
- **No CI/CD pipeline**: Manual builds and deployments

---

## 🚀 Phase 1: Event-Driven Architecture with Message Broker

### 1.1 Message Broker Selection & Implementation

#### **Recommendation: RabbitMQ** (Primary Choice)
**Why RabbitMQ?**
- ✅ Perfect for microservice event-driven patterns
- ✅ Easier learning curve than Kafka
- ✅ Native support for Spring Boot (Spring AMQP)
- ✅ Built-in UI for message monitoring
- ✅ Supports multiple messaging patterns (pub/sub, routing, topics)
- ✅ Lightweight and fast for transactional events

**Alternative: Apache Kafka** (If you want streaming/big data showcase)
- Better for high-throughput event streaming
- More impressive on resume for data-intensive roles
- Requires more infrastructure (Zookeeper/KRaft)

#### Implementation Steps

**1.1.1 Infrastructure Setup**
```yaml
# docker-compose.yml (create at root)
version: '3.8'

services:
  rabbitmq:
    image: rabbitmq:3.13-management-alpine
    container_name: bottle-drop-rabbitmq
    ports:
      - "5672:5672"    # AMQP port
      - "15672:15672"  # Management UI
    environment:
      RABBITMQ_DEFAULT_USER: admin
      RABBITMQ_DEFAULT_PASS: admin123
    volumes:
      - rabbitmq-data:/var/lib/rabbitmq
    networks:
      - bottle-drop-network
    healthcheck:
      test: rabbitmq-diagnostics -q ping
      interval: 10s
      timeout: 5s
      retries: 5

  redis:
    image: redis:7.2-alpine
    container_name: bottle-drop-redis
    ports:
      - "6379:6379"
    volumes:
      - redis-data:/data
    networks:
      - bottle-drop-network

  prometheus:
    image: prom/prometheus:latest
    container_name: bottle-drop-prometheus
    ports:
      - "9090:9090"
    volumes:
      - ./monitoring/prometheus.yml:/etc/prometheus/prometheus.yml
      - prometheus-data:/prometheus
    networks:
      - bottle-drop-network

  grafana:
    image: grafana/grafana:latest
    container_name: bottle-drop-grafana
    ports:
      - "3001:3000"
    environment:
      - GF_SECURITY_ADMIN_PASSWORD=admin
    volumes:
      - grafana-data:/var/lib/grafana
    networks:
      - bottle-drop-network

  loki:
    image: grafana/loki:latest
    container_name: bottle-drop-loki
    ports:
      - "3100:3100"
    volumes:
      - ./monitoring/loki-config.yml:/etc/loki/local-config.yaml
    networks:
      - bottle-drop-network

volumes:
  rabbitmq-data:
  redis-data:
  prometheus-data:
  grafana-data:

networks:
  bottle-drop-network:
    driver: bridge
```

**1.1.2 Add RabbitMQ Dependencies to All Services**
```xml
<!-- Add to each service's pom.xml -->
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-amqp</artifactId>
</dependency>
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-stream-rabbit</artifactId>
</dependency>
```

**1.1.3 Event-Driven Communication Patterns to Implement**

Create a shared event library: `common-events` module
```java
// Event types to implement
public class StockLevelChangedEvent {
    private String barId;
    private String drinkId;
    private int currentLevel;
    private int threshold;
    private Instant timestamp;
}

public class RestockRequestedEvent {
    private String barId;
    private String drinkId;
    private int quantity;
    private RequestPriority priority;
}

public class RestockCompletedEvent {
    private String requestId;
    private String barId;
    private Map<String, Integer> deliveredItems;
}

public class DropPointFullEvent {
    private String dropPointId;
    private int currentCapacity;
    private Location location;
}

public class EmptiesCollectedEvent {
    private String dropPointId;
    private int bottleCount;
    private String warehouseId;
}

public class EventCreatedEvent {
    private String eventId;
    private String eventName;
    private List<BarConfiguration> bars;
    private List<DropPointConfiguration> dropPoints;
}
```

**1.1.4 Replace OpenFeign Calls with Events**

| Current Synchronous Flow | New Asynchronous Event Flow |
|--------------------------|------------------------------|
| Bar → REST → Warehouse (restock) | Bar publishes `RestockRequestedEvent` → Warehouse consumes & publishes `RestockCompletedEvent` |
| DropPoint → REST → Warehouse (empties) | DropPoint publishes `EmptiesCollectedEvent` → Warehouse consumes |
| EventPlanner → REST → All Services | EventPlanner publishes `EventCreatedEvent` → All services consume & initialize |
| Any Service → REST → Users (auth check) | Keep REST for synchronous auth, but add `UserUpdatedEvent` for cache invalidation |

**1.1.5 Configuration Template**
```yaml
# config-repo/bar-service.yml
spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: admin123
    listener:
      simple:
        retry:
          enabled: true
          initial-interval: 1000
          max-attempts: 3
          multiplier: 2.0

  cloud:
    stream:
      bindings:
        # Outbound
        stockLevelChanged-out-0:
          destination: stock.events
          content-type: application/json
        restockRequested-out-0:
          destination: restock.requests
        
        # Inbound
        restockCompleted-in-0:
          destination: restock.completed
          group: bar-service-group
          
      rabbit:
        bindings:
          restockCompleted-in-0:
            consumer:
              exchange-type: topic
              binding-routing-key: restock.completed.*
```

### 1.2 Benefits Achieved
- ⚡ **Real-time async communication** between services
- 🔄 **Decoupling**: Services don't need to know about each other
- 📈 **Scalability**: Messages queued during high load
- 🛡️ **Resilience**: Services can fail independently without cascading
- 📊 **Event Sourcing**: Full audit trail of business events

---

## 🔒 Phase 2: Security Hardening

### 2.1 Complete JWT Implementation

**2.1.1 Centralized Authentication Service Enhancement**
- Implement refresh token rotation
- Add token blacklisting (using Redis)
- Implement MFA (Time-based OTP)
- Add OAuth2 integration (Google, GitHub)

**2.1.2 Service-to-Service Authentication**
```xml
<!-- Add Spring Cloud Security -->
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-security</artifactId>
</dependency>
```

**Implementation:**
- Generate service-specific JWTs for inter-service calls
- Store service credentials in Config Server with encryption
- Implement request signing for event messages

**2.1.3 API Gateway Security Enhancements**
```java
// Add rate limiting with Redis
@Configuration
public class RateLimitConfig {
    @Bean
    public RedisRateLimiter redisRateLimiter() {
        return new RedisRateLimiter(100, 200); // replenishRate, burstCapacity
    }
    
    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        return builder.routes()
            .route("bar-service", r -> r
                .path("/api/bars/**")
                .filters(f -> f
                    .requestRateLimiter(c -> c.setRateLimiter(redisRateLimiter()))
                    .circuitBreaker(c -> c.setName("barServiceCB")))
                .uri("lb://bar-service"))
            .build();
    }
}
```

**2.1.4 HTTPS/TLS Configuration**
- Generate self-signed certificates for dev
- Configure TLS termination at Gateway
- Document certificate management for production

**2.1.5 Security Headers**
```yaml
# Gateway security headers
spring:
  cloud:
    gateway:
      default-filters:
        - SecureHeaders
        - name: RequestRateLimiter
          args:
            redis-rate-limiter.replenishRate: 10
            redis-rate-limiter.burstCapacity: 20
```

### 2.2 Secrets Management
- Move all credentials to environment variables
- Implement Spring Cloud Config encryption
- Document Azure Key Vault integration (for cloud deployment)
- Add `.env` templates with dummy values

### 2.3 Input Validation & Sanitization
```java
// Add to all DTOs
@NotNull
@Min(1) @Max(1000)
private Integer quantity;

@Pattern(regexp = "^[a-zA-Z0-9-]+$")
private String barId;

@Valid
private List<DrinkItem> items;
```

### 2.4 CORS Configuration
```java
// Centralized CORS in Gateway
@Configuration
public class CorsConfig {
    @Bean
    public CorsWebFilter corsWebFilter() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:5173"));
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(Arrays.asList("*"));
        config.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        
        return new CorsWebFilter(source);
    }
}
```

---

## 🛡️ Phase 3: Resilience & Fault Tolerance

### 3.1 Circuit Breaker Implementation (Resilience4j)

**Dependencies:**
```xml
<dependency>
    <groupId>org.springframework.cloud</groupId>
    <artifactId>spring-cloud-starter-circuitbreaker-resilience4j</artifactId>
</dependency>
<dependency>
    <groupId>io.github.resilience4j</groupId>
    <artifactId>resilience4j-spring-boot3</artifactId>
</dependency>
```

**Configuration:**
```yaml
resilience4j:
  circuitbreaker:
    configs:
      default:
        slidingWindowSize: 10
        minimumNumberOfCalls: 5
        failureRateThreshold: 50
        waitDurationInOpenState: 10000
        permittedNumberOfCallsInHalfOpenState: 3
    instances:
      warehouseService:
        baseConfig: default
      userService:
        baseConfig: default

  retry:
    configs:
      default:
        maxAttempts: 3
        waitDuration: 1000
        exponentialBackoffMultiplier: 2
        
  bulkhead:
    configs:
      default:
        maxConcurrentCalls: 25
        maxWaitDuration: 100
```

**Usage Example:**
```java
@Service
public class BarService {
    
    @CircuitBreaker(name = "warehouseService", fallbackMethod = "requestRestockFallback")
    @Retry(name = "warehouseService")
    public void requestRestock(RestockRequest request) {
        // Original logic
        eventPublisher.publishRestockRequest(request);
    }
    
    private void requestRestockFallback(RestockRequest request, Exception ex) {
        log.error("Restock request failed, storing for later retry", ex);
        // Store in local queue or dead letter queue
        restockQueueRepository.save(new PendingRestock(request));
    }
}
```

### 3.2 Health Checks & Readiness Probes
```yaml
# Each service
management:
  health:
    livenessState:
      enabled: true
    readinessState:
      enabled: true
  endpoint:
    health:
      probes:
        enabled: true
      show-details: always
      group:
        readiness:
          include: readinessState, db, rabbit
        liveness:
          include: livenessState, ping
```

```java
// Custom health indicator
@Component
public class RabbitMQHealthIndicator implements HealthIndicator {
    @Autowired
    private RabbitTemplate rabbitTemplate;
    
    @Override
    public Health health() {
        try {
            rabbitTemplate.execute(channel -> {
                channel.queueDeclarePassive("health-check-queue");
                return true;
            });
            return Health.up().withDetail("rabbitmq", "Available").build();
        } catch (Exception e) {
            return Health.down(e).build();
        }
    }
}
```

### 3.3 Graceful Shutdown
```yaml
server:
  shutdown: graceful

spring:
  lifecycle:
    timeout-per-shutdown-phase: 30s
```

---

## 📊 Phase 4: Advanced Observability

### 4.1 Distributed Tracing with Micrometer + Zipkin

**Add Dependencies:**
```xml
<dependency>
    <groupId>io.micrometer</groupId>
    <artifactId>micrometer-tracing-bridge-brave</artifactId>
</dependency>
<dependency>
    <groupId>io.zipkin.reporter2</groupId>
    <artifactId>zipkin-reporter-brave</artifactId>
</dependency>
```

**Docker Compose Addition:**
```yaml
  zipkin:
    image: openzipkin/zipkin:latest
    container_name: bottle-drop-zipkin
    ports:
      - "9411:9411"
    networks:
      - bottle-drop-network
```

**Configuration:**
```yaml
management:
  tracing:
    sampling:
      probability: 1.0  # 100% for development
  zipkin:
    tracing:
      endpoint: http://localhost:9411/api/v2/spans
```

### 4.2 Correlation IDs & MDC Logging
```java
@Component
public class CorrelationIdFilter implements WebFilter {
    
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest()
            .getHeaders()
            .getFirst("X-Correlation-ID");
            
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }
        
        MDC.put("correlationId", correlationId);
        
        exchange.getResponse()
            .getHeaders()
            .add("X-Correlation-ID", correlationId);
            
        return chain.filter(exchange)
            .doFinally(signalType -> MDC.clear());
    }
}
```

**Logback Configuration:**
```xml
<configuration>
    <appender name="LOKI" class="com.github.loki4j.logback.Loki4jAppender">
        <http>
            <url>http://localhost:3100/loki/api/v1/push</url>
        </http>
        <format>
            <label>
                <pattern>app=${spring.application.name},host=${HOSTNAME},level=%level</pattern>
            </label>
            <message>
                <pattern>[%X{correlationId}] %d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n</pattern>
            </message>
        </format>
    </appender>
    
    <root level="INFO">
        <appender-ref ref="LOKI" />
    </root>
</configuration>
```

### 4.3 Custom Metrics
```java
@Service
public class MetricsService {
    private final Counter restockRequestCounter;
    private final Timer stockUpdateTimer;
    
    public MetricsService(MeterRegistry registry) {
        this.restockRequestCounter = Counter.builder("bar.restock.requests")
            .description("Number of restock requests")
            .tag("service", "bar-service")
            .register(registry);
            
        this.stockUpdateTimer = Timer.builder("bar.stock.update.duration")
            .description("Time taken to update stock")
            .register(registry);
    }
    
    public void recordRestockRequest() {
        restockRequestCounter.increment();
    }
    
    public void recordStockUpdate(Runnable operation) {
        stockUpdateTimer.record(operation);
    }
}
```

### 4.4 Grafana Dashboards
Create custom dashboards for:
- Service health and availability
- Request throughput and latency (p50, p95, p99)
- Circuit breaker status
- Message queue depths and processing rates
- Database connection pool usage
- JVM metrics (heap, GC pauses)

### 4.5 Alerting Rules (Prometheus)
```yaml
# monitoring/alert-rules.yml
groups:
  - name: bottle_drop_alerts
    rules:
      - alert: ServiceDown
        expr: up{job="bottle-drop-services"} == 0
        for: 2m
        annotations:
          summary: "Service {{ $labels.instance }} is down"
          
      - alert: HighErrorRate
        expr: rate(http_server_requests_seconds_count{status=~"5.."}[5m]) > 0.05
        for: 5m
        annotations:
          summary: "High error rate on {{ $labels.service }}"
          
      - alert: CircuitBreakerOpen
        expr: resilience4j_circuitbreaker_state{state="open"} == 1
        for: 1m
        annotations:
          summary: "Circuit breaker open for {{ $labels.name }}"
```

---

## 🔄 Phase 5: Data Consistency & Transaction Management

### 5.1 Saga Pattern Implementation

**Create Saga Orchestrator Service**
```java
@Service
public class RestockSaga {
    
    @Transactional
    public void executeRestockSaga(RestockRequest request) {
        String sagaId = UUID.randomUUID().toString();
        
        SagaInstance saga = sagaRepository.save(new SagaInstance(sagaId, "RESTOCK"));
        
        try {
            // Step 1: Reserve stock in warehouse
            SagaStep reserveStep = executeStep(saga, () -> 
                warehouseEventPublisher.reserveStock(request));
            
            // Step 2: Arrange delivery
            SagaStep deliveryStep = executeStep(saga, () ->
                deliveryEventPublisher.scheduleDelivery(request));
            
            // Step 3: Update bar inventory (on delivery confirmation)
            // This is event-driven, waiting for DeliveryCompletedEvent
            
            saga.setStatus(SagaStatus.COMPLETED);
            
        } catch (SagaException e) {
            saga.setStatus(SagaStatus.COMPENSATING);
            compensate(saga);
        }
        
        sagaRepository.save(saga);
    }
    
    private void compensate(SagaInstance saga) {
        // Execute compensating transactions in reverse order
        saga.getSteps().stream()
            .sorted(Comparator.comparing(SagaStep::getOrder).reversed())
            .forEach(step -> {
                if (step.getStatus() == StepStatus.COMPLETED) {
                    compensatingActions.get(step.getType()).execute(step);
                }
            });
    }
}
```

### 5.2 Outbox Pattern for Reliable Event Publishing
```java
@Entity
public class OutboxEvent {
    @Id
    private String id;
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    private LocalDateTime createdAt;
    private boolean published;
}

@Service
public class OutboxPublisher {
    
    @Transactional
    public void saveEventAndPublish(DomainEvent event, Runnable businessLogic) {
        // Execute business logic and save event in same transaction
        businessLogic.run();
        
        OutboxEvent outboxEvent = new OutboxEvent(
            event.getEventId(),
            event.getAggregateType(),
            event.getAggregateId(),
            event.getClass().getSimpleName(),
            jsonMapper.writeValueAsString(event),
            LocalDateTime.now(),
            false
        );
        
        outboxRepository.save(outboxEvent);
    }
    
    @Scheduled(fixedDelay = 1000)
    public void publishPendingEvents() {
        List<OutboxEvent> pending = outboxRepository.findByPublishedFalse();
        
        pending.forEach(event -> {
            try {
                rabbitTemplate.convertAndSend(
                    event.getEventType(),
                    event.getPayload()
                );
                event.setPublished(true);
                outboxRepository.save(event);
            } catch (Exception e) {
                log.error("Failed to publish outbox event: " + event.getId(), e);
            }
        });
    }
}
```

### 5.3 Idempotency Keys
```java
@Entity
public class IdempotencyKey {
    @Id
    private String key;
    private String response;
    private LocalDateTime createdAt;
    private LocalDateTime expiresAt;
}

@Aspect
@Component
public class IdempotencyAspect {
    
    @Around("@annotation(idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint pjp, Idempotent idempotent) {
        String key = extractIdempotencyKey(pjp);
        
        Optional<IdempotencyKey> existing = idempotencyRepository.findById(key);
        if (existing.isPresent()) {
            return deserialize(existing.get().getResponse());
        }
        
        Object result = pjp.proceed();
        
        idempotencyRepository.save(new IdempotencyKey(
            key,
            serialize(result),
            LocalDateTime.now(),
            LocalDateTime.now().plusHours(24)
        ));
        
        return result;
    }
}
```

### 5.4 Event Versioning Strategy
```java
// Version 1
public class StockLevelChangedEventV1 {
    private String barId;
    private int newLevel;
}

// Version 2 (with backward compatibility)
public class StockLevelChangedEventV2 {
    private String barId;
    private String drinkId; // New field
    private int newLevel;
    private int previousLevel; // New field
    
    public static StockLevelChangedEventV2 fromV1(StockLevelChangedEventV1 v1) {
        return new StockLevelChangedEventV2(v1.getBarId(), null, v1.getNewLevel(), 0);
    }
}

// Event consumer with version handling
@RabbitListener(queues = "stock.events")
public void handleStockEvent(Message message) {
    String version = message.getMessageProperties()
        .getHeader("event-version");
        
    if ("v1".equals(version)) {
        StockLevelChangedEventV1 v1 = mapper.readValue(
            message.getBody(), StockLevelChangedEventV1.class);
        handleEvent(StockLevelChangedEventV2.fromV1(v1));
    } else {
        StockLevelChangedEventV2 v2 = mapper.readValue(
            message.getBody(), StockLevelChangedEventV2.class);
        handleEvent(v2);
    }
}
```

---

## 🧪 Phase 6: Testing Strategy

### 6.1 Unit Tests (Target: 80% coverage)
```java
@ExtendWith(MockitoExtension.class)
class BarServiceTest {
    
    @Mock
    private BarRepository barRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @InjectMocks
    private BarService barService;
    
    @Test
    void shouldPublishRestockEventWhenStockLow() {
        // Given
        Bar bar = createBarWithLowStock();
        when(barRepository.findById(anyString())).thenReturn(Optional.of(bar));
        
        // When
        barService.sellDrink("bar-1", "drink-1", 5);
        
        // Then
        verify(eventPublisher).publish(any(RestockRequestedEvent.class));
    }
}
```

### 6.2 Integration Tests with Testcontainers
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-testcontainers</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>rabbitmq</artifactId>
    <scope>test</scope>
</dependency>
<dependency>
    <groupId>org.testcontainers</groupId>
    <artifactId>postgresql</artifactId>
    <scope>test</scope>
</dependency>
```

```java
@SpringBootTest
@Testcontainers
class BarServiceIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");
    
    @Container
    static RabbitMQContainer rabbitMQ = new RabbitMQContainer("rabbitmq:3.13-management-alpine");
    
    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.rabbitmq.host", rabbitMQ::getHost);
        registry.add("spring.rabbitmq.port", rabbitMQ::getAmqpPort);
    }
    
    @Test
    void shouldProcessRestockRequestEndToEnd() {
        // Test complete flow with real DB and message broker
    }
}
```

### 6.3 Contract Testing with Spring Cloud Contract
```groovy
// contracts/restockRequest.groovy
Contract.make {
    description "Should publish restock request when stock is low"
    
    input {
        triggeredBy('publishRestockRequest()')
    }
    
    outputMessage {
        sentTo('restock.requests')
        body([
            barId: $(consumer('bar-1'), producer(regex('[a-z0-9-]+'))),
            drinkId: $(consumer('drink-1'), producer(regex('[a-z0-9-]+'))),
            quantity: $(consumer(10), producer(regex('[0-9]+')))
        ])
        headers {
            messagingContentType(applicationJson())
        }
    }
}
```

### 6.4 End-to-End Tests with Playwright
```typescript
// e2e/bar-restock.spec.ts
import { test, expect } from '@playwright/test';

test('bartender can view low stock alert and request restock', async ({ page }) => {
  await page.goto('http://localhost:3000/bar');
  
  await page.fill('#username', 'bartender1');
  await page.fill('#password', 'password');
  await page.click('button[type="submit"]');
  
  await expect(page.locator('.low-stock-alert')).toBeVisible();
  
  await page.click('button.request-restock');
  
  await expect(page.locator('.toast-success')).toContainText('Restock requested');
  
  // Verify event was published (check mock RabbitMQ or backend logs)
});
```

### 6.5 Performance Testing with Gatling
```scala
// simulations/BarServiceSimulation.scala
class BarServiceSimulation extends Simulation {
  
  val httpProtocol = http
    .baseUrl("http://localhost:8080")
    .acceptHeader("application/json")
    .authorizationHeader("Bearer ${jwt_token}")
  
  val scn = scenario("Bar Stock Operations")
    .exec(http("Get Bar Stock")
      .get("/api/bars/bar-1/stock"))
    .pause(1)
    .exec(http("Sell Drink")
      .post("/api/bars/bar-1/sell")
      .body(StringBody("""{"drinkId": "drink-1", "quantity": 2}"""))
      .check(status.is(200)))
  
  setUp(
    scn.inject(
      rampUsersPerSec(1) to 100 during (60 seconds),
      constantUsersPerSec(100) during (120 seconds)
    )
  ).protocols(httpProtocol)
}
```

### 6.6 Chaos Engineering with Chaos Monkey
```xml
<dependency>
    <groupId>de.codecentric</groupId>
    <artifactId>chaos-monkey-spring-boot</artifactId>
</dependency>
```

```yaml
chaos:
  monkey:
    enabled: true
    watcher:
      controller: true
      service: true
      repository: true
    assaults:
      level: 5
      latencyActive: true
      latencyRangeStart: 1000
      latencyRangeEnd: 5000
      exceptionsActive: true
      killApplicationActive: false
```

---

## 🚢 Phase 7: DevOps & CI/CD Pipeline

### 7.1 GitHub Actions Workflow

**7.1.1 Build & Test Pipeline**
```yaml
# .github/workflows/ci.yml
name: CI Pipeline

on:
  push:
    branches: [ main, develop ]
  pull_request:
    branches: [ main ]

jobs:
  build-and-test:
    runs-on: ubuntu-latest
    
    services:
      postgres:
        image: postgres:15-alpine
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
      
      rabbitmq:
        image: rabbitmq:3.13-alpine
        ports:
          - 5672:5672
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: '21'
          distribution: 'temurin'
          cache: maven
      
      - name: Build Config Server
        run: |
          cd servers/config-server
          mvn clean install -DskipTests
      
      - name: Build Eureka Server
        run: |
          cd servers/eureka-server
          mvn clean install -DskipTests
      
      - name: Build Gateway
        run: |
          cd servers/gateway
          mvn clean install -DskipTests
      
      - name: Build and Test Microservices
        run: |
          services=("bar-service/backend" "warehouse-service/backend" "users-service/backend" "event-planner-service/eventPlanner" "drop-points-service/Backend/dropPointsSys")
          for service in "${services[@]}"; do
            echo "Building $service..."
            cd $service
            mvn clean verify
            cd -
          done
      
      - name: Generate Test Coverage Report
        run: |
          mvn jacoco:report-aggregate
      
      - name: Upload Coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          files: ./target/site/jacoco-aggregate/jacoco.xml
      
      - name: SonarQube Scan
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: |
          mvn sonar:sonar \
            -Dsonar.projectKey=bottle-drop \
            -Dsonar.host.url=${{ secrets.SONAR_HOST_URL }}
      
      - name: Build Docker Images
        run: |
          docker compose -f docker-compose.dev.yml build
      
      - name: Run Integration Tests
        run: |
          docker compose -f docker-compose.dev.yml up -d
          sleep 30
          npm run test:e2e
          docker compose -f docker-compose.dev.yml down
```

**7.1.2 Security Scanning**
```yaml
# .github/workflows/security.yml
name: Security Scan

on:
  schedule:
    - cron: '0 0 * * 0' # Weekly
  workflow_dispatch:

jobs:
  dependency-check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: OWASP Dependency Check
        uses: dependency-check/Dependency-Check_Action@main
        with:
          project: 'bottle-drop'
          path: '.'
          format: 'ALL'
      
      - name: Upload Results
        uses: actions/upload-artifact@v3
        with:
          name: dependency-check-report
          path: reports/
  
  trivy-scan:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Run Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'sarif'
          output: 'trivy-results.sarif'
      
      - name: Upload to GitHub Security
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'
```

**7.1.3 Deployment Pipeline**
```yaml
# .github/workflows/deploy.yml
name: Deploy to Production

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      
      - name: Build and Push Docker Images
        run: |
          echo ${{ secrets.DOCKER_PASSWORD }} | docker login -u ${{ secrets.DOCKER_USERNAME }} --password-stdin
          docker compose -f docker-compose.prod.yml build
          docker compose -f docker-compose.prod.yml push
      
      - name: Deploy to Kubernetes
        uses: azure/k8s-deploy@v4
        with:
          namespace: bottle-drop-prod
          manifests: |
            k8s/deployment.yml
            k8s/service.yml
            k8s/ingress.yml
          images: |
            yourusername/bar-service:${{ github.ref_name }}
            yourusername/warehouse-service:${{ github.ref_name }}
      
      - name: Run Smoke Tests
        run: |
          npm run test:smoke -- --env=production
      
      - name: Notify Deployment
        uses: slackapi/slack-github-action@v1
        with:
          channel-id: 'deployments'
          slack-message: "✅ Bottle Drop ${{ github.ref_name }} deployed to production"
        env:
          SLACK_BOT_TOKEN: ${{ secrets.SLACK_BOT_TOKEN }}
```

### 7.2 Docker Optimization

**7.2.1 Multi-stage Dockerfile**
```dockerfile
# bar-service/backend/Dockerfile
FROM maven:3.9-eclipse-temurin-21-alpine AS builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn package -DskipTests

FROM eclipse-temurin:21-jre-alpine

RUN addgroup -S spring && adduser -S spring -G spring
USER spring:spring

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8080

HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8080/actuator/health || exit 1

ENTRYPOINT ["java", \
  "-XX:+UseContainerSupport", \
  "-XX:MaxRAMPercentage=75.0", \
  "-Djava.security.egd=file:/dev/./urandom", \
  "-jar", "app.jar"]
```

### 7.3 Kubernetes Deployment (Optional Showcase)

**7.3.1 Deployment Manifest**
```yaml
# k8s/bar-service-deployment.yml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: bar-service
  labels:
    app: bar-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: bar-service
  template:
    metadata:
      labels:
        app: bar-service
    spec:
      containers:
      - name: bar-service
        image: yourusername/bar-service:latest
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_PROFILES_ACTIVE
          value: "prod"
        - name: EUREKA_CLIENT_SERVICEURL_DEFAULTZONE
          value: "http://eureka-server:8761/eureka/"
        - name: SPRING_RABBITMQ_HOST
          value: "rabbitmq-service"
        envFrom:
        - secretRef:
            name: bar-service-secrets
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 10
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: bar-service
spec:
  selector:
    app: bar-service
  ports:
    - protocol: TCP
      port: 8080
      targetPort: 8080
  type: ClusterIP
```

**7.3.2 Horizontal Pod Autoscaler**
```yaml
apiVersion: autoscaling/v2
kind: HorizontalPodAutoscaler
metadata:
  name: bar-service-hpa
spec:
  scaleTargetRef:
    apiVersion: apps/v1
    kind: Deployment
    name: bar-service
  minReplicas: 2
  maxReplicas: 10
  metrics:
  - type: Resource
    resource:
      name: cpu
      target:
        type: Utilization
        averageUtilization: 70
  - type: Resource
    resource:
      name: memory
      target:
        type: Utilization
        averageUtilization: 80
```

### 7.4 Infrastructure as Code (Terraform)

```hcl
# terraform/main.tf
terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 3.0"
    }
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "bottle_drop" {
  name     = "bottle-drop-rg"
  location = "West Europe"
}

resource "azurerm_kubernetes_cluster" "aks" {
  name                = "bottle-drop-aks"
  location            = azurerm_resource_group.bottle_drop.location
  resource_group_name = azurerm_resource_group.bottle_drop.name
  dns_prefix          = "bottle-drop"

  default_node_pool {
    name       = "default"
    node_count = 3
    vm_size    = "Standard_D2_v2"
  }

  identity {
    type = "SystemAssigned"
  }
}

resource "azurerm_container_registry" "acr" {
  name                = "bottledropacr"
  resource_group_name = azurerm_resource_group.bottle_drop.name
  location            = azurerm_resource_group.bottle_drop.location
  sku                 = "Standard"
  admin_enabled       = true
}
```

---

## 📚 Phase 8: Documentation & Showcase

### 8.1 Comprehensive README.md

```markdown
# 🍾 Bottle Drop Platform - Enterprise Microservices Architecture

[![Build Status](https://github.com/yourusername/bottle-drop/workflows/CI/badge.svg)]()
[![Code Coverage](https://codecov.io/gh/yourusername/bottle-drop/branch/main/graph/badge.svg)]()
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)]()

> A production-grade microservices platform for festival beverage logistics management

## 🎯 Project Highlights

This project demonstrates **enterprise-level microservice architecture** with:

- ✅ **Event-Driven Architecture** with RabbitMQ
- ✅ **Service Discovery** (Eureka) & **API Gateway** (Spring Cloud Gateway)
- ✅ **Distributed Tracing** (Zipkin + Micrometer)
- ✅ **Resilience Patterns** (Circuit Breaker, Retry, Bulkhead)
- ✅ **Centralized Configuration** (Spring Cloud Config)
- ✅ **JWT Authentication** with role-based access control
- ✅ **Observability Stack** (Prometheus, Grafana, Loki)
- ✅ **Redis Caching** for performance optimization
- ✅ **Saga Pattern** for distributed transactions
- ✅ **CI/CD Pipeline** with GitHub Actions
- ✅ **Containerization** with Docker & Kubernetes deployment
- ✅ **Comprehensive Testing** (Unit, Integration, E2E, Contract, Performance)

## 🏗️ Architecture Overview

[Include architecture diagram]

### Services
- **Bar Service**: Manages bar inventory, pricing, and sales
- **Warehouse Service**: Central inventory and replenishment
- **Drop Point Service**: Bottle return point management
- **Event Planner Service**: Event configuration and orchestration
- **Users Service**: Authentication and user management

### Infrastructure
- **API Gateway**: Single entry point with rate limiting
- **Eureka Server**: Service discovery
- **Config Server**: Externalized configuration
- **RabbitMQ**: Asynchronous messaging
- **Redis**: Caching layer
- **PostgreSQL**: Per-service databases

### Observability
- **Prometheus**: Metrics collection
- **Grafana**: Visualization dashboards
- **Loki**: Centralized logging
- **Zipkin**: Distributed tracing

## 🚀 Quick Start

[Include docker-compose startup instructions]

## 🧪 Running Tests

```bash
# Unit tests
mvn test

# Integration tests
mvn verify

# E2E tests
npm run test:e2e

# Performance tests
mvn gatling:test
```

## 📊 Monitoring

- **Grafana**: http://localhost:3001 (admin/admin)
- **Prometheus**: http://localhost:9090
- **Zipkin**: http://localhost:9411
- **RabbitMQ Management**: http://localhost:15672 (admin/admin123)

## 🛠️ Technology Stack

**Backend**: Java 21, Spring Boot 3.x, Spring Cloud  
**Frontend**: Vue 3, Vite, Tailwind CSS  
**Messaging**: RabbitMQ  
**Databases**: PostgreSQL  
**Caching**: Redis  
**Monitoring**: Prometheus, Grafana, Loki, Zipkin  
**Testing**: JUnit 5, Testcontainers, Playwright, Gatling  
**CI/CD**: GitHub Actions, Docker, Kubernetes

## 📖 Documentation

- [Architecture Decision Records](docs/adr/)
- [API Documentation](docs/api/)
- [Deployment Guide](docs/deployment/)
- [Development Guide](docs/development/)

## 📝 License

MIT
```

### 8.2 Architecture Decision Records (ADRs)

Create `docs/adr/` folder with:
- `001-message-broker-selection.md`
- `002-database-per-service.md`
- `003-saga-pattern-for-distributed-transactions.md`
- `004-jwt-authentication-strategy.md`
- `005-api-gateway-pattern.md`

**Example ADR:**
```markdown
# ADR 001: Message Broker Selection - RabbitMQ

## Status
Accepted

## Context
We need asynchronous, event-driven communication between microservices to:
- Decouple services
- Enable real-time updates
- Improve system resilience
- Support Saga pattern for distributed transactions

## Decision
We will use **RabbitMQ** as our message broker.

## Consequences

### Positive
- Native Spring AMQP support
- Multiple exchange types (direct, topic, fanout)
- Built-in management UI
- Easier to learn and operate than Kafka
- Perfect for transactional events
- Lower infrastructure overhead

### Negative
- Not ideal for high-throughput streaming (Kafka would be better)
- Less suitable for log aggregation use cases

## Alternatives Considered
- **Apache Kafka**: Better for streaming, but overkill for our use case
- **AWS SQS**: Cloud-only, vendor lock-in
- **ActiveMQ**: Older technology, less community support
```

### 8.3 API Documentation with OpenAPI/Swagger

```java
// Add to each service
@Configuration
public class OpenApiConfig {
    
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("Bar Service API")
                .version("1.0")
                .description("API for managing bar inventory and sales")
                .contact(new Contact()
                    .name("Your Name")
                    .email("your.email@example.com")
                    .url("https://github.com/yourusername")))
            .components(new Components()
                .addSecuritySchemes("bearer-jwt", new SecurityScheme()
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")));
    }
}
```

**Access at**: `http://localhost:8080/swagger-ui.html`

### 8.4 Postman Collection

Export Postman collection with:
- All API endpoints
- Environment variables
- Pre-request scripts for JWT token
- Example requests and responses

### 8.5 Video Demonstration (Optional)

Record a 5-10 minute demo showing:
1. System startup with docker-compose
2. Monitoring dashboards (Grafana, Zipkin)
3. RabbitMQ message flow
4. Circuit breaker activation
5. Distributed tracing across services
6. Frontend features

Upload to YouTube and link in README.

### 8.6 Blog Post / Medium Article

Write a technical blog post explaining:
- Architecture decisions
- Challenges faced and solutions
- Performance optimizations
- Lessons learned

---

## 🎓 Phase 9: Advanced Features (Optional Enhancements)

### 9.1 GraphQL API
```xml
<dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-graphql</artifactId>
</dependency>
```

### 9.2 gRPC for Internal Communication
```xml
<dependency>
    <groupId>net.devh</groupId>
    <artifactId>grpc-spring-boot-starter</artifactId>
</dependency>
```

### 9.3 Service Mesh (Istio)
- Traffic management
- Security (mTLS)
- Observability

### 9.4 WebSocket for Real-time Updates
```java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {
    
    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        config.enableSimpleBroker("/topic");
        config.setApplicationDestinationPrefixes("/app");
    }
    
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")
            .setAllowedOrigins("*")
            .withSockJS();
    }
}

// Publish real-time stock updates
@Autowired
private SimpMessagingTemplate messagingTemplate;

public void notifyStockUpdate(StockUpdate update) {
    messagingTemplate.convertAndSend("/topic/stock-updates", update);
}
```

### 9.5 Event Sourcing with Axon Framework
```xml
<dependency>
    <groupId>org.axonframework</groupId>
    <artifactId>axon-spring-boot-starter</artifactId>
</dependency>
```

### 9.6 CQRS Pattern
Separate read and write models for complex queries

### 9.7 Multi-tenancy Support
Support multiple festivals/organizations

### 9.8 Internationalization (i18n)
Multi-language support in frontends

### 9.9 Rate Limiting at Service Level
```java
@RateLimiter(name = "barService", fallbackMethod = "rateLimitFallback")
public ResponseEntity<?> getBarStock(String barId) {
    // Implementation
}
```

### 9.10 Feature Flags with Unleash
```java
if (unleash.isEnabled("new-stock-algorithm")) {
    // Use new algorithm
} else {
    // Use old algorithm
}
```

---

## 📋 Implementation Roadmap

### Sprint 1 (Weeks 1-2): Foundation
- [ ] Set up docker-compose with all infrastructure services
- [ ] Add RabbitMQ to all services (dependencies + config)
- [ ] Create common-events shared library
- [ ] Implement basic event publishing/consuming

### Sprint 2 (Weeks 3-4): Event-Driven Patterns
- [ ] Replace REST calls with events (bar ↔ warehouse)
- [ ] Implement Outbox pattern
- [ ] Add event versioning
- [ ] Implement idempotency

### Sprint 3 (Weeks 5-6): Security Hardening
- [ ] Complete JWT implementation across all services
- [ ] Add service-to-service authentication
- [ ] Implement rate limiting in Gateway
- [ ] Add input validation to all endpoints
- [ ] Configure CORS and HTTPS

### Sprint 4 (Weeks 7-8): Resilience
- [ ] Add Resilience4j to all services
- [ ] Implement circuit breakers for external calls
- [ ] Add retry logic with exponential backoff
- [ ] Implement Saga pattern for critical flows
- [ ] Add health checks and graceful shutdown

### Sprint 5 (Weeks 9-10): Observability
- [ ] Set up Zipkin and distributed tracing
- [ ] Implement correlation IDs
- [ ] Configure Loki logging
- [ ] Create Grafana dashboards
- [ ] Set up Prometheus alerting rules
- [ ] Add custom metrics

### Sprint 6 (Weeks 11-12): Testing
- [ ] Write unit tests (target 80% coverage)
- [ ] Implement integration tests with Testcontainers
- [ ] Add contract tests
- [ ] Create E2E tests with Playwright
- [ ] Set up performance tests with Gatling

### Sprint 7 (Weeks 13-14): CI/CD
- [ ] Create GitHub Actions workflows
- [ ] Set up security scanning
- [ ] Configure Docker multi-stage builds
- [ ] Create Kubernetes manifests
- [ ] Set up automated deployment

### Sprint 8 (Weeks 15-16): Documentation & Polish
- [ ] Write comprehensive README
- [ ] Create Architecture Decision Records
- [ ] Generate OpenAPI documentation
- [ ] Create Postman collection
- [ ] Record demo video
- [ ] Write technical blog post

---

## 🎤 Interview Talking Points

When discussing this project with interviewers, emphasize:

### Microservices Architecture
- "I implemented a **fully distributed microservice architecture** with 5 independent services, each with its own database following the database-per-service pattern"
- "Used **Spring Cloud Netflix Eureka** for service discovery and **Spring Cloud Gateway** for API gateway pattern"

### Event-Driven Design
- "Implemented **event-driven architecture** using RabbitMQ with Spring Cloud Stream to decouple services"
- "Used **Outbox pattern** to ensure reliable event publishing with transactional consistency"
- "Implemented **Saga pattern** for distributed transactions across multiple services"

### Resilience & Fault Tolerance
- "Integrated **Resilience4j** for circuit breakers, retry logic, and bulkheads to handle service failures gracefully"
- "Implemented fallback mechanisms to ensure system degradation instead of complete failure"

### Security
- "Implemented **JWT-based authentication** with role-based access control (RBAC)"
- "Added **service-to-service authentication** to prevent unauthorized internal calls"
- "Configured **rate limiting** and **CORS** at the API Gateway level"

### Observability
- "Set up comprehensive **observability stack** with Prometheus for metrics, Grafana for visualization, and Loki for centralized logging"
- "Implemented **distributed tracing** with Zipkin and Micrometer to track requests across service boundaries"
- "Added **correlation IDs** to trace requests throughout the system"

### DevOps
- "Built **complete CI/CD pipeline** with GitHub Actions including automated testing, security scanning, and deployment"
- "Containerized all services with **multi-stage Docker builds** for optimized image sizes"
- "Created **Kubernetes manifests** with HPA for auto-scaling based on load"

### Testing
- "Achieved **80%+ code coverage** with unit tests, integration tests using Testcontainers, and E2E tests with Playwright"
- "Implemented **contract testing** to ensure API compatibility between services"
- "Used **Gatling** for performance testing and identified bottlenecks"

### Data Consistency
- "Handled **eventual consistency** challenges in distributed systems using the Saga pattern"
- "Implemented **idempotency** to handle duplicate event processing"
- "Used **event versioning** for backward compatibility"

---

## 📊 Expected Outcomes

After completing this improvement plan, your project will demonstrate:

1. **Technical Breadth**: 20+ modern technologies and patterns
2. **Production Readiness**: Proper error handling, monitoring, security
3. **Best Practices**: Clean code, comprehensive testing, documentation
4. **Scalability**: Horizontal scaling, caching, async communication
5. **Maintainability**: Clear architecture, ADRs, API documentation

This project will be a **standout portfolio piece** that showcases your ability to design and implement **enterprise-grade distributed systems**.

---

## 🔗 Recommended Reading

- *Building Microservices* by Sam Newman
- *Designing Data-Intensive Applications* by Martin Kleppmann
- *Enterprise Integration Patterns* by Gregor Hohpe
- *Release It!* by Michael T. Nygard
- *Spring Microservices in Action* by John Carnell

---

## 📞 Getting Help

- Spring Cloud Documentation: https://spring.io/projects/spring-cloud
- RabbitMQ Tutorials: https://www.rabbitmq.com/getstarted.html
- Resilience4j Guide: https://resilience4j.readme.io/
- Microservices Patterns: https://microservices.io/

---

**Last Updated**: December 19, 2025  
**Version**: 1.0  
**Author**: [Your Name]