
for event-planner-service:

Create event table with the following columns:
- id (primary key, auto-increment)
- name (string, not null)
- date (date, not null)
- location (string, not null)
- description (text, nullable)
- created_at (timestamp, default current timestamp)
- updated_at (timestamp, default current timestamp on update current timestamp)
- organizer_name (string, not null)
- organizer_email (string, not null)
- organizer_phone (string, nullable)
- attendees_count (integer, default 0)
- max_attendees (integer, nullable)
- is_public (boolean, default true)
- status (enum: 'scheduled', 'cancelled', 'completed', 'running' default 'scheduled')

Create Bar table with the following columns:
- id (primary key, auto-increment)
- name (string, not null)
- location (string, not null)
- capacity (integer, not null)
- created_at (timestamp, default current timestamp)
- updated_at (timestamp, default current timestamp on update current timestamp)
- event_id (integer, foreign key referencing event(id))
- event_occupancy (boolean, default false)
- assigned_staff (string, nullable)

Create BarStock table with the following columns:
- id (primary key, auto-increment)
- bar_id (integer, foreign key referencing Bar(id))
- item_name (string, not null)
- quantity (integer, not null)
- created_at (timestamp, default current timestamp)
- updated_at (timestamp, default current timestamp on update current timestamp)

Create DropPoint table with the following columns:
- id (primary key, auto-increment)
- name (string, not null)
- location (string, not null)
- event_id (integer, foreign key referencing event(id))
- event_occupancy (boolean, default false)
- assigned_staff (string, nullable)
- created_at (timestamp, default current timestamp)
- updated_at (timestamp, default current timestamp on update current timestamp)


On EventPlannerService startup, ensure all tables are created if they do not exist and inserted into mySQL database. event_db.

if eventPlanner workflow is that:
1. Create a new event.
2. Create Bar and assign it to the event. an staff member will be assigned to manage the Bar. user will be fetching available staff from UserService via REST API using sync call OpenFeign client.
3. Create BarStock items for the Bar. Items will be fetched from WarehouseService inventory via REST API using sync call OpenFeign client.
4. Create DropPoint and assign it to the event.
once user clicks "Start Event" button, update event status to 'running' and set event_occupancy to true for both Bar and DropPoint associated with the event.
and publish an event to RabbitMQ exchange for both Bar and DropPoint creation with relevant details. with payload containing Bar/DropPoint id, name, location, capacity.
and an event will also be published to RabbitMQ exchange to UserService to notify the assigned staff member about their assignment with relevant details. with payload containing staff id, barid/dropPointId.

now rewrite and reinvent the whole event-planner-service codebase to accomplish above requirements 