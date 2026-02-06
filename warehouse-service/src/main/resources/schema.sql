-- Beverage Stock Table
CREATE TABLE IF NOT EXISTS beverage_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    beverage_type VARCHAR(255) NOT NULL UNIQUE,
    quantity INT NOT NULL DEFAULT 0,
    min_stock_level INT DEFAULT 10,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    version BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_beverage_type ON beverage_stock(beverage_type);

-- Drop Point Collection Table
CREATE TABLE IF NOT EXISTS drop_point_collection (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drop_point_id BIGINT NOT NULL,
    location VARCHAR(255),
    bottle_count INT NOT NULL DEFAULT 0,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    notified_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    accepted_at TIMESTAMP NULL,
    collected_at TIMESTAMP NULL,
    notes VARCHAR(500)
);

CREATE INDEX IF NOT EXISTS idx_drop_point_id ON drop_point_collection(drop_point_id);
CREATE INDEX IF NOT EXISTS idx_status ON drop_point_collection(status);

-- Empty Bottle Inventory Table
CREATE TABLE IF NOT EXISTS empty_bottle_inventory (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    drop_point_id BIGINT NOT NULL UNIQUE,
    drop_point_location VARCHAR(255),
    total_bottles_collected INT NOT NULL DEFAULT 0,
    last_collection_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE UNIQUE INDEX IF NOT EXISTS idx_inv_drop_point_id ON empty_bottle_inventory(drop_point_id);

-- Supply Requests Table (Event-Driven)
CREATE TABLE IF NOT EXISTS supply_requests (
    id BINARY(16) PRIMARY KEY,
    bar_id BINARY(16) NOT NULL,
    bar_name VARCHAR(255),
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL,
    rejection_reason VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_supply_bar_id ON supply_requests(bar_id);
CREATE INDEX IF NOT EXISTS idx_supply_status ON supply_requests(status);

-- Supply Request Items Table
CREATE TABLE IF NOT EXISTS supply_request_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    supply_request_id BINARY(16) NOT NULL,
    product_id BINARY(16),
    product_name VARCHAR(255),
    quantity INT,
    CONSTRAINT fk_supply_request FOREIGN KEY (supply_request_id) REFERENCES supply_requests(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_req_items_req_id ON supply_request_items(supply_request_id);