-- EVENTS
INSERT INTO EVENTS (EVENT_ID, DATE, DURATION, LOCATION, NAME, STATUS) VALUES
  (1, '2025-06-27', 1, 'Oberhausen', 'Event1', 'PLANNED');

-- EVENT_BEVERAGES
INSERT INTO EVENT_BEVERAGES (EVENT_ID, ID, NAME, PRICE) VALUES
  (1, 1, 'Coke',   2.5),
  (1, 2, 'Sprite', 2.5),
  (1, 3, 'Water',  1.0);

-- BAR_PLANS (2 bars)
INSERT INTO BAR_PLANS (BAR_ID, BAR_NAME, LOCATION, TOTAL_CAPACITY, EVENT_ID) VALUES
  (2, 'Main Bar', 'East Wing', 50, 1),
  (3, 'VIP Bar',  'West Hall', 20, 1);

-- DROP_POINT_PLANS (2 drop points)
INSERT INTO DROP_POINT_PLANS (DROP_POINT_ID, CAPACITY, LOCATION, EVENT_ID) VALUES
  (2, 100, 'North Gate', 1),
  (3,  80, 'South Exit', 1);

-- BAR_BEVERAGE_STOCK (for both bars)
INSERT INTO BAR_BEVERAGE_STOCK (BAR_ID, QUANTITY, BEVERAGE_ID) VALUES
  (2, 3, 1),
  (2, 3, 2),
  (2, 3, 3),
  (3, 5, 1),
  (3, 5, 2),
  (3, 5, 3);

-- ─────────────────────────────────────────────────────────────────────────
-- Reseed identity columns so new inserts don’t collide with hard-coded IDs
-- ─────────────────────────────────────────────────────────────────────────

ALTER TABLE EVENTS            ALTER COLUMN EVENT_ID            RESTART WITH 2;
ALTER TABLE EVENT_BEVERAGES  ALTER COLUMN ID                  RESTART WITH 4;
ALTER TABLE BAR_PLANS        ALTER COLUMN BAR_ID              RESTART WITH 4;
ALTER TABLE DROP_POINT_PLANS ALTER COLUMN DROP_POINT_ID       RESTART WITH 4;
