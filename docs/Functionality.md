# Functional Requirements

## REQ-01: Manage a Central Warehouse
**Description:** The system shall store and allow retrieval of the number of beverages stored (by type) and the number of stored empties in the central warehouse.
**Priority:** High
**Actors:** System

## REQ-02: Manage Bars
**Description:** The system shall store and manage bar details including name, location, maximum capacity, and the stock of different beverage types.
**Priority:** High
**Actors:** System

## REQ-03: Manage Bottle Drop Points
**Description:** The system shall store and manage details of bottle drop points, including location, capacity, and current stock of empties.
**Priority:** High
**Actors:** System

## REQ-04: View Warehouse Data and Status
**Description:** Warehouse staff should be able to view up-to-date information about the warehouse, including beverage stock levels, supply request queue, and empty bottle inventory.
**Priority:** High
**Actors:** Warehouse Staff

## REQ-05: View Bar Data and Status
**Description:** Bartenders should be able to access real-time information about their bar, including stock levels, usage logs, and supply request history.
**Priority:** High
**Actors:** Bartenders

## REQ-06: Bars Request Automatic Stock Replenishment
**Description:** Bars should automatically trigger stock replenishment requests from the warehouse when the quantity of any beverage type falls to or below 5 units. The bartender has a 10-second cancellation window before the request is submitted.
**Priority:** High
**Actors:** System, Bartender

## REQ-07: Serve Drinks at a Bar
**Description:** Bartenders should be able to serve drinks at a bar, with the system decrementing the bar's stock, recording a usage log entry with timestamp, and calculating total served quantities per drink type.
**Priority:** High
**Actors:** Bartender

## REQ-08: Return Empty Bottles to a Drop Point
**Description:** Visitors should be able to return empty bottles at a designated drop point, with the system recording the quantity and updating the current empties stock.
**Priority:** High
**Actors:** Visitors

## REQ-09: Notify Warehouse of Full Drop Point
**Description:** When a drop point reaches full capacity, the system shall automatically notify the warehouse by publishing an asynchronous collection event.
**Priority:** High
**Actors:** System

## REQ-10: View All Bottle Drop Points Data and Status
**Description:** Warehouse staff should be able to view information about all bottle drop points, including their locations, capacities, current stock, and collection status.
**Priority:** High
**Actors:** Warehouse Staff

## REQ-11: Prevent Bottle Returns to Full Drop Points
**Description:** The system should prevent visitors from returning empty bottles to a drop point that has reached its full capacity.
**Priority:** High
**Actors:** System

## REQ-12: Empty Bottle Drop Points to Central Warehouse
**Description:** Warehouse staff should be able to accept, collect, and complete collection tasks for full drop points. The system transfers the count of collected empties to the central empty bottle inventory and resets the drop point.
**Priority:** High
**Actors:** Warehouse Staff

## REQ-13: Manage Events
**Description:** Event planners should be able to create and configure events by specifying details such as name, date, location, description, organizer information, and attendee count. Events follow a lifecycle: SCHEDULED → RUNNING → COMPLETED (or CANCELLED).
**Priority:** High
**Actors:** Event Planners

## REQ-14: Set Up Bars with Location, Capacity, and Initial Stock
**Description:** Event planners should be able to configure bars for an event, specifying each bar's name, location, capacity, and initial beverage stock plan.
**Priority:** High
**Actors:** Event Planners

## REQ-15: Set Up Drop Points with Location and Capacity
**Description:** Event planners should be able to configure drop points for an event, specifying the number needed, their locations, and the capacity (maximum number of empties they can hold).
**Priority:** High
**Actors:** Event Planners

## REQ-16: Manage Users
**Description:** Administrators should be able to register new users, assign roles (Admin, Manager, Operator) scoped to specific services and resources, and manage user accounts.
**Priority:** High
**Actors:** Administrators

## REQ-17: User Login and Authentication
**Description:** All users must authenticate to access the system. The system shall verify credentials and issue JWT access tokens with HTTP-only refresh token cookies.
**Priority:** High
**Actors:** All Users

## REQ-18: Role-Based Authorization
**Description:** The system shall enforce role-based access control, with roles scoped per service and optionally per resource (e.g., `BAR:OPERATOR:42`). JWT claims carry role assignments evaluated at the gateway and service level.
**Priority:** High
**Actors:** System

## REQ-19: Manage User Profile
**Description:** Users should be able to view and update their own profile information, such as name, email, and password.
**Priority:** Medium
**Actors:** All Users

## REQ-20: Real-Time Notifications
**Description:** The system shall push real-time updates via WebSocket to connected dashboards when domain events occur (stock changes, supply request updates, drop point status changes). The gateway relays RabbitMQ events to clients through a ticket-authenticated WebSocket connection.
**Priority:** High
**Actors:** System

---

# Domain Model

## Entities per Bounded Context

### Identity & Access
- **User**: `id`, `username`, `email`, `firstName`, `lastName`, `passwordHash`, `enabled`, `locked`, `mfaEnabled`
- **Role**: `id`, `name`, `global`, `permissions[]`
- **Permission**: `id`, `code`, `description`
- **Service**: `id`, `code`, `description`
- **UserRoleAssignment**: `id`, `user`, `role`, `service`, `resourceId`

### Event Planning
- **Event**: `id`, `name`, `date`, `location`, `status`, `bars[]`, `dropPoints[]`
- **Bar**: `id`, `name`, `location`, `capacity`, `stocks[]`
- **BarStock**: `id`, `itemName`, `quantity`
- **DropPoint**: `id`, `name`, `location`, `capacity`

### Bar Operations
- **Bar**: `id`, `name`, `location`, `maxCapacity`, `stockItems[]`
- **BarStockItem**: `id`, `barId`, `productName`, `quantity`, `updatedAt`
- **SupplyRequest**: `id`, `barId`, `status`, `items[]`, `createdAt`
- **UsageLog**: `id`, `barId`, `productName`, `quantity`, `timestamp`

### Drop Point
- **DropPoint**: `id`, `location`, `capacity`, `current_empties_stock`, `status`

### Warehouse
- **BeverageStock**: `id`, `beverageType`, `quantity`, `reservedQuantity`, `usedQuantity`, `minStockLevel`, `version`
- **SupplyRequest**: `id`, `barId`, `status`, `items[]`, `requestedQuantity`, `fulfilledQuantity`
- **DropPointCollection**: `id`, `dropPointId`, `bottleCount`, `status`, `notifiedAt`, `collectedAt`
- **EmptyBottleInventory**: `id`, `dropPointId`, `totalBottlesCollected`, `lastCollectionAt`

## Cross-Context Relationships

- **Event Planning → Bar Operations**: Event configuration is projected to runtime bars via `BarBootstrapEvent`
- **Event Planning → Drop Point**: Event configuration is projected via `DropPointBootstrapEvent`
- **Bar Operations → Warehouse**: Supply requests flow via `SupplyRequestCreatedEvent`
- **Drop Point → Warehouse**: Collection notifications flow via `DropPointCollectionEvent`
- **Warehouse → Bar / Drop Point**: Status updates flow back via corresponding lifecycle events

> For detailed class diagrams see [`ddd-tactical-design.md`](ddd-tactical-design.md).
