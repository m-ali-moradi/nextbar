Domain Storytelling (Detailed)
Event-Planner Context (Planning / Upstream)
Story 1: Event Planner Authentication

Actors: Event Planner, System
The event planner logs into the system using valid credentials.
The system authenticates the user, validates the EVENT_PLANNER role, and grants access to planning features.

Story 2: Create Event

Actors: Event Planner, System
The event planner creates a new event by providing:

Event name

Date and time

Venue and location

Expected number of visitors

The system creates the event in DRAFT status and assigns a unique event identifier.

Story 3: Create Bars for Event

Actors: Event Planner, System
The event planner creates one or more bars associated with the event.
Each bar is linked to the event but is not yet operational.

Story 4: Configure Bar Properties

Actors: Event Planner, System
For each bar, the event planner defines:

Bar name

Physical location

Maximum capacity

The system validates capacity constraints and stores the configuration as planning data.

Story 5: Create Drop Points for Event

Actors: Event Planner, System
The event planner creates bottle drop points associated with the event.
Each drop point is linked to the event and remains inactive until the event starts.

Story 6: Configure Drop Point Properties

Actors: Event Planner, System
For each drop point, the event planner defines:

Drop point name

Location

Capacity

The system validates drop point capacities and stores the configuration.

Story 7: Define Initial Bar Stock

Actors: Event Planner, System, Warehouse
The event planner defines the initial stock plan for each bar:

Drink type

Planned quantity

The system retrieves available beverage definitions from the Warehouse context and validates the request.
This data represents planned stock, not real inventory.

Story 8: Assign Bartenders to Bars

Actors: Event Planner, System
The event planner assigns bartenders to bars.
The system:

Filters users by BARTENDER role

Checks availability status

Prevents double assignments

Assignments are stored and locked once the event is finalized.

Story 9: Assign Stockists to Drop Points

Actors: Event Planner, System
The event planner assigns stockists to drop points.
Only users with:

STOCKIST role

Available status
can be assigned.

Story 10: Finalize Event Setup

Actors: Event Planner, System
The event planner finalizes the event.
The system validates:

Bars and drop points exist

Staff assignments are complete

Initial stock plans are defined

The event status changes from DRAFT to FINALIZED.

Story 11: Notify Event Staff

Actors: System
The system sends notifications:

Bartenders receive bar assignment details

Stockists receive drop point assignment details

Notifications are sent via email.

Story 12: Notify Warehouse

Actors: System, Warehouse
The system publishes initial stock requirements to the Warehouse context using a Published Language.
Warehouse staff are informed of expected stock preparation needs.

Warehouse Context
Story 1: Warehouse Staff Authentication

Actors: Warehouse Staff, System
Warehouse staff log in and are authenticated with the WAREHOUSE_STAFF role.

Story 2: Fulfill Initial Event Stock

Actors: Warehouse Staff, System
Warehouse staff view initial stock plans created by the Event Planner.
They prepare and deliver stock to the assigned bars.

Story 3: Handle Bar Replenishment Requests

Actors: Warehouse Staff, System
Warehouse staff view replenishment requests submitted by bars.
Requests are processed in order and marked as:

IN_PROGRESS

DELIVERED

Story 4: Process Empty Bottles

Actors: Warehouse Staff, System
Warehouse staff receive empty bottles from drop points.
The system updates warehouse inventory and tracks returned quantities.

Story 5: Manage Warehouse Inventory

Actors: Warehouse Staff, System
Warehouse staff monitor inventory levels and reorder stock when thresholds are reached.

Bar Context
Story 1: Bartender Authentication

Actors: Bartender, System
Bartenders log in and can only access bars assigned to them.

Story 2: View Bar Stock

Actors: Bartender, System
Bartenders view current stock levels per drink for their assigned bar.

Story 3: Serve Drinks

Actors: Bartender, System
A bartender serves a drink to a customer.
The system:

Decrements the stock

Logs the served drink with timestamp and quantity

Story 4: Manual Supply Request

Actors: Bartender, System
Bartenders manually request additional stock when needed.
The request is created with status REQUESTED.

Story 5: Auto Supply Request

Actors: System
When stock falls below a defined threshold:

The system automatically creates a supply request

A toast notification is shown with a 10-second cancel window

Story 6: Cancel Supply Request

Actors: Bartender, System
If the request is still in REQUESTED status, the bartender can cancel it.

Story 7: Track Supply Request Status

Actors: Bartender, System
Bartenders view request status updates:

REQUESTED

IN_PROGRESS

DELIVERED

Story 8: Receive Stock Delivery

Actors: Bartender, System
When stock is delivered:

The system increases bar inventory

Marks the request as DELIVERED

Story 9: View Usage Logs

Actors: Bartender, System
Bartenders view detailed usage logs:

Drink type

Quantity

Timestamp

Drop-Point Context
Story 1: Stockist Authentication

Actors: Stockist, System
Stockists log in and can access only assigned drop points.

Story 2: Monitor Drop Point Level

Actors: Stockist, System
Stockists view the current fill level of drop points.

Story 3: Empty Drop Point

Actors: Stockist, System, Warehouse
When full, the stockist empties the drop point.
The system reports returned bottles to the Warehouse context.

Story 4: Report Drop Point Status

Actors: System
The system reports drop point status (NORMAL, FULL) to the Event Planner context.

Users Context
Story 1: User Registration

Actors: User, System
Users register and are assigned roles:
Admin, Event Planner, Bartender, Stockist, Warehouse Staff, Visitor.

Story 2: Authentication

Actors: User, System
Users authenticate using JWT-based login.

Story 3: Authorization

Actors: System
The system enforces role-based access control for all features.

Story 4: Manage Profile and Availability

Actors: User, System
Users manage:

Profile details

Availability status

Story 5: Password Recovery

Actors: User, System
Users reset passwords via secure recovery flow.

Story 6: Admin User Management

Actors: Admin, System
Admins manage users, roles, and accounts.

Story 7: Audit and Security Logging

Actors: System
All critical actions are logged for audit and compliance.