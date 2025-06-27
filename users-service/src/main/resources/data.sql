-- Visitor
INSERT INTO users (username, email, password, role, active)
VALUES ('visitor1', 'visitor@example.com', 'visitorpass', 'VISITOR', true);

-- Bartender
INSERT INTO users (username, email, password, role, active)
VALUES ('bartender1', 'bartender@example.com', 'bartenderpass', 'BARTENDER', true);

-- Warehouse Staff
INSERT INTO users (username, email, password, role, active)
VALUES ('warehouse1', 'warehouse@example.com', 'warehousepass', 'WAREHOUSE_STAFF', true);

-- Stockist
INSERT INTO users (username, email, password, role, active)
VALUES ('stockist1', 'stockist@example.com', 'stockistpass', 'STOCKIST', true);

-- Planner (Admin)
INSERT INTO users (username, email, password, role, active)
VALUES ('planner1', 'planner@example.com', 'plannerpass', 'PLANNER', true);
