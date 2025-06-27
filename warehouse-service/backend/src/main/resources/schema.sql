CREATE TABLE beverage_stock (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  beverage_type VARCHAR(255),
  quantity INT
);

CREATE TABLE empty_bottle_stock (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  drop_point_id BIGINT NOT NULL,
  drop_point_location VARCHAR(255),
  quantity INT NOT NULL
);

CREATE TABLE drop_point_records (
  id BIGINT PRIMARY KEY,
  location VARCHAR(255),
  current_empties INTEGER,
  status VARCHAR(50)
);