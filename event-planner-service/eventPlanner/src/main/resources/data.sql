-- EVENTS
INSERT INTO EVENTS (EVENT_ID, DATE, DURATION, LOCATION, NAME, STATUS) VALUES
  (1, '2025-06-27', 1, 'Oberhausen', 'Event1', 'PLANNED');

-- EVENT_BEVERAGES
INSERT INTO EVENT_BEVERAGES (EVENT_ID, ID, NAME, PRICE) VALUES
  (1, 1, 'Fanta',   25),
  (1, 2, 'Red Bull', 25),
  (1, 3, 'Beer',  10);

-- BAR_PLANS (2 bars)
INSERT INTO BAR_PLANS (BAR_ID, BAR_NAME, LOCATION, TOTAL_CAPACITY, EVENT_ID) VALUES
  (2, 'Main Bar', 'East Wing', 300, 1),
  (3, 'VIP Bar',  'West Hall', 200, 1);

-- DROP_POINT_PLANS (2 drop points)
INSERT INTO DROP_POINT_PLANS (DROP_POINT_ID, CAPACITY, LOCATION, EVENT_ID) VALUES
  (2, 100, 'North Gate', 1),
  (3,  80, 'South Exit', 1);

-- BAR_BEVERAGE_STOCK (for both bars)
INSERT INTO BAR_BEVERAGE_STOCK (BAR_ID, QUANTITY, BEVERAGE_ID) VALUES
  (2, 60, 1),
  (2, 40, 2),
  (2, 50, 3),
  (3, 40, 1),
  (3, 30, 2),
  (3, 25, 3);

-- ─────────────────────────────────────────────────────────────────────────
-- Reseed identity columns so new inserts don’t collide with hard-coded IDs
-- ─────────────────────────────────────────────────────────────────────────

ALTER TABLE EVENTS            ALTER COLUMN EVENT_ID            RESTART WITH 2;
ALTER TABLE EVENT_BEVERAGES  ALTER COLUMN ID                  RESTART WITH 4;
ALTER TABLE BAR_PLANS        ALTER COLUMN BAR_ID              RESTART WITH 4;
ALTER TABLE DROP_POINT_PLANS ALTER COLUMN DROP_POINT_ID       RESTART WITH 4;
