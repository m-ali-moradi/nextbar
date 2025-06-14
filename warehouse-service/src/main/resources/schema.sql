CREATE TABLE beverage_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    beverage_type VARCHAR(255),
    quantity INT
);

CREATE TABLE empty_bottle_stock (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    quantity INT
);
