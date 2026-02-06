<p align="center">
  <img src="docs/Images/nextbar-logo.png" alt="NextBar Logo" width="200"/>
</p>

<h1 align="center">🍺 NextBar - Event Beverage Management System</h1>

<p align="center">
  <strong>A microservices-based platform for managing bars, beverages, and bottle recycling at events</strong>
</p>

<p align="center">
  <img src="https://img.shields.io/badge/Java-21-orange?logo=openjdk" alt="Java 21"/>
  <img src="https://img.shields.io/badge/Spring%20Boot-3.x-brightgreen?logo=springboot" alt="Spring Boot"/>
  <img src="https://img.shields.io/badge/Vue.js-3-4FC08D?logo=vuedotjs" alt="Vue 3"/>
  <img src="https://img.shields.io/badge/Spring%20Cloud-2023.x-blue?logo=spring" alt="Spring Cloud"/>
  <img src="https://img.shields.io/badge/License-MIT-yellow" alt="License"/>
</p>

---

## 📋 Overview

NextBar is a distributed system designed to streamline beverage management at large-scale events like festivals and concerts. It provides real-time inventory tracking, automated supply chain management, and efficient bottle recycling workflows.

### Key Features

- 🍻 **Bar Management** - Track stock levels, serve drinks, auto-request replenishment
- 📦 **Central Warehouse** - Manage beverage inventory and empty bottle storage
- ♻️ **Bottle Drop Points** - Handle returns, capacity monitoring, fullness alerts
- 🎉 **Event Planning** - Configure events with bars, drop points, and beverage menus
- 👥 **User Management** - Role-based access control (Admin, Manager, Operator)
- 🔔 **Real-time Updates** - WebSocket-powered live notifications

---

## 🏗️ Architecture

```
┌─────────────────────────────────────────────────────────────────────────┐
│                         Vue.js Frontend (5174)                          │
└─────────────────────────────────┬───────────────────────────────────────┘
                                  │ HTTP/WebSocket
┌─────────────────────────────────▼───────────────────────────────────────┐
│                      Spring Cloud Gateway (8080)                        │
│                    JWT Auth │ Routing │ WebSocket                       │
└───────┬─────────┬─────────┬─────────┬─────────┬─────────────────────────┘
        │         │         │         │         │
   ┌────▼───┐ ┌───▼────┐ ┌──▼───┐ ┌───▼───┐ ┌───▼────┐
   │ Users  │ │  Bar   │ │Event │ │ Drop  │ │Warehouse│
   │ 8090   │ │ 8081   │ │ 8082 │ │ 8083  │ │  8085  │
   └────────┘ └────────┘ └──────┘ └───────┘ └─────────┘
        │         │         │         │         │
   ┌────▼─────────▼─────────▼─────────▼─────────▼─────┐
   │        Eureka (8761)  │  Config Server (8888)    │
   └──────────────────────────────────────────────────┘
```

> 📐 See [docs/architecture.puml](docs/architecture.puml) for the complete PlantUML diagram.

---

## 🛠️ Tech Stack

| Layer | Technology |
|-------|------------|
| **Backend** | Java 21, Spring Boot 3.x, Spring Cloud |
| **Service Discovery** | Netflix Eureka |
| **Configuration** | Spring Cloud Config |
| **API Gateway** | Spring Cloud Gateway (WebFlux) |
| **Inter-Service Comm** | OpenFeign |
| **Resilience** | Resilience4j (Circuit Breakers) |
| **Security** | JWT Authentication |
| **Frontend** | Vue 3, Pinia, Axios, TypeScript |
| **Real-time** | WebSocket (Reactive) |
| **Database** | H2 (dev), MySQL (users) |

---

## 🚀 Quick Start

### Prerequisites

- Java 21+
- Maven 3.8+
- Node.js 18+
- MySQL (for users-service)

### 1. Clone the Repository

```bash
git clone https://github.com/your-org/nextbar.git
cd nextbar
```

### 2. Start Infrastructure (Required Order)

```bash
# 1. Config Server
cd servers/config-server && mvn spring-boot:run &

# 2. Eureka Server (wait for Config Server)
cd servers/eureka-server && mvn spring-boot:run &

# 3. Gateway
cd servers/gateway && mvn spring-boot:run &
```

### 3. Start Business Services

```bash
# Users Service (MySQL required)
cd users-service && mvn spring-boot:run &

# Other services (any order)
cd bar-service && mvn spring-boot:run &
cd event-planner-service && mvn spring-boot:run &
cd drop-points-service && mvn spring-boot:run &
cd warehouse-service && mvn spring-boot:run &
```

### 4. Start Frontend

```bash
cd servers/frontend
npm install
npm run dev
```

### 5. Access the Application

| Service | URL |
|---------|-----|
| **Frontend** | http://localhost:5174 |
| **Gateway API** | http://localhost:8080 |
| **Eureka Dashboard** | http://localhost:8761 |
| **Swagger UI (per service)** | http://localhost:{port}/swagger-ui.html |

---

## 📁 Project Structure

```
nextbar/
├── 📂 servers/
│   ├── config-server/      # Centralized configuration
│   ├── eureka-server/      # Service discovery
│   ├── gateway/            # API Gateway + WebSocket
│   └── frontend/           # Vue.js SPA
├── 📂 bar-service/         # Bar management microservice
├── 📂 event-planner-service/ # Event configuration
├── 📂 drop-points-service/ # Bottle return points
├── 📂 warehouse-service/   # Central inventory
├── 📂 users-service/       # Auth & user management
├── 📂 config-repo/         # YAML configuration files
├── 📂 docs/                # Documentation & diagrams
└── 📄 QUICK-START.md       # Detailed setup guide
```

---

## 🔐 Authentication

NextBar uses JWT tokens for authentication:

1. **Login** via `POST /api/auth/login`
2. Receive JWT token in response
3. Include token in subsequent requests: `Authorization: Bearer <token>`

### Roles & Permissions

| Role | Scope | Access |
|------|-------|--------|
| `Admin` | Global | Full access to all services |
| `Manager` | Per-service | Manage service + operators |
| `Operator` | Per-resource | Assigned resource only |

---

## 🔌 API Endpoints

All requests go through the gateway at `http://localhost:8080`:

| Route | Service | Auth |
|-------|---------|------|
| `/api/auth/**` | Users | ❌ |
| `/api/users/**` | Users | ❌ |
| `/api/bars/**` | Bar | ✅ |
| `/api/events/**` | Event Planner | ✅ |
| `/api/droppoints/**` | Drop Points | ✅ |
| `/api/warehouse/**` | Warehouse | ✅ |
| `/ws/events` | WebSocket | ❌ |

---

## 📡 Real-time Events

Connect to `ws://localhost:8080/ws/events` for live updates:

| Event | Description |
|-------|-------------|
| `SUPPLY_REQUEST_UPDATED` | Supply request status changed |
| `BAR_STOCK_UPDATED` | Bar inventory changed |
| `WAREHOUSE_STOCK_UPDATED` | Warehouse inventory changed |
| `DROPPOINT_STATUS_CHANGED` | Drop point capacity changed |
| `EVENT_UPDATED` | Event configuration changed |

---

## 🧪 Testing

### Run Unit Tests

```bash
# All services
mvn test

# Specific service
cd bar-service && mvn test
```

### Test with Swagger

1. Open Swagger UI for any service
2. Login at Users Service to get JWT
3. Click **Authorize** → Enter `Bearer <token>`
4. Test endpoints directly

---

## 📖 Documentation

| Document | Description |
|----------|-------------|
| [QUICK-START.md](QUICK-START.md) | Detailed setup instructions |
| [docs/02. Functionality.md](docs/02.%20Functionality.md) | Functional requirements |
| [docs/architecture.puml](docs/architecture.puml) | PlantUML architecture diagram |
| [docs/domain story telling.md](docs/domain%20story%20telling.md) | Domain modeling |

---

## 🤝 Contributing

1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Commit changes: `git commit -m 'Add amazing feature'`
4. Push to branch: `git push origin feature/amazing-feature`
5. Open a Pull Request

---

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

---

## 👥 Team

Developed as part of the Software Intensive Solutions course at FH Dortmund.

---

<p align="center">
  <strong>Made with ☕ and 🍺</strong>
</p>
