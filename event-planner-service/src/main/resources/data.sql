-- EVENTS
INSERT INTO events (
  id,
  name,
  date,
  location,
  description,
  created_at,
  updated_at,
  organizer_name,
  organizer_email,
  organizer_phone,
  attendees_count,
  max_attendees,
  is_public,
  status
) VALUES (
  1,
  'Event1',
  '2025-06-27',
  'Oberhausen',
  'Sample kickoff event',
  CURRENT_TIMESTAMP,
  CURRENT_TIMESTAMP,
  'Default Organizer',
  'organizer@example.com',
  NULL,
  0,
  500,
  true,
  'SCHEDULED'
);

-- BARS (2 bars)
INSERT INTO bars (
  id,
  name,
  location,
  capacity,
  created_at,
  updated_at,
  event_id,
  event_occupancy,
  assigned_staff
) VALUES
  (1, 'Main Bar', 'East Wing', 300, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, false, NULL),
  (2, 'VIP Bar',  'West Hall', 200, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 1, false, NULL);

-- DROP POINTS (2 drop points)
INSERT INTO drop_points (
  id,
  name,
  location,
  event_id,
  event_occupancy,
  assigned_staff,
  created_at,
  updated_at
) VALUES
  (1, 'North Gate', 'North Gate', 1, false, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 'South Exit', 'South Exit', 1, false, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- BAR STOCKS (for both bars)
INSERT INTO bar_stocks (
  id,
  bar_id,
  item_name,
  quantity,
  created_at,
  updated_at
) VALUES
  (1, 1, 'Fanta', 60, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (2, 1, 'Red Bull', 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (3, 1, 'Beer', 50, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (4, 2, 'Fanta', 40, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (5, 2, 'Red Bull', 30, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
  (6, 2, 'Beer', 25, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Reseed identity columns so new inserts don't collide with hard-coded IDs
ALTER TABLE events AUTO_INCREMENT = 2;
ALTER TABLE bars AUTO_INCREMENT = 3;
ALTER TABLE drop_points AUTO_INCREMENT = 3;
ALTER TABLE bar_stocks AUTO_INCREMENT = 7;
