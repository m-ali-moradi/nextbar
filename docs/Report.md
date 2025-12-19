## Overview
- Bottle Drop platform built as a polyrepo-style monorepo of Spring Boot microservices with Vue frontends. Supports festival beverage logistics: manage bars, central warehouse, bottle drop points, and event setup. Implements JWT-based auth and role-based access control (admins/planners/bartenders/warehouse/visitors).

## Backends (Java, Spring Boot, Maven)
- bar-service/backend: Manages bar inventory and pricing; exposes REST endpoints for drink stock, sales, and low-stock auto-replenishment from warehouse.
- warehouse-service/backend: Tracks beverage stock and empty-bottle stock at the central warehouse; services replenishment requests and receives empties.
- drop-points-service/Backend: Handles bottle drop points (capacity, current level) and supports reporting full drop points and emptying them to warehouse.
- event-planner-service/eventPlanner: Event configuration (events, bar layout, drink lists, capacities, initial stocks, bottle drop points) and orchestration between services.
- users-service/backend: User management, authentication, and role provisioning; participates in JWT auth flows.
- servers/config-server, servers/eureka-server, servers/gateway: Spring Cloud Config for centralized settings (pulls from config-repo), Eureka for service discovery, and API Gateway entrypoint.

## Frontends (Vue 3 + Vite)
- bar-service/frontend: Operator UI for bartenders to view bar stock, sell drinks, and trigger restock; Tailwind-based styling.
- drop-points-service/Frontend/drop-points: UI for stockists/visitors to view or report bottle drop point status.
- event-planner-service/frontend (root frontend folder): Planner UI to configure events, bars, and drop points.
- warehouse-service/frontend: Warehouse dashboard for stock and empties visibility; Vitest config present for unit tests.

## Shared Configuration and Docs
- config-repo/*.yml: Per-service Spring configuration consumed by config-server.
- JWT-AUTHENTICATION-GUIDE.md and QUICK-START-JWT.md: How to obtain and test JWTs; scripts start-all-services.ps1 and test-jwt-auth.ps1 assist local bring-up and health checks.
- Bottle Drop - Documentation/*: Functional requirements, domain model, and architecture links; mirrors the behaviors implemented across the services and UIs.

## Functional Coverage (mapped from codebase + docs)
- Manage central warehouse inventory and empties; fulfill bar replenishment requests.
- Manage bars with per-drink stock and pricing; decrement on sales; auto-replenish on thresholds.
- Manage bottle drop points (location, capacity); block returns when full; report full; transfer empties to warehouse.
- Event planning: create/update/delete events; configure bars and bottle drop points per event; seed initial stock and capacities.
- User lifecycle with JWT auth and role-based permissions across all flows.
- Notifications/alerts implied for low stock and full drop points (per requirements docs).

## Architecture Notes
- Microservices registered via Eureka, fronted by a Gateway; configuration externalized via Config Server to config-repo.
- Each backend packaged as Spring Boot jar (Java 21). Frontends built with Vite; Tailwind present in bar UI.
- Scripted local startup and health polling provided; ports typically on 8080/3000 per service defaults.
