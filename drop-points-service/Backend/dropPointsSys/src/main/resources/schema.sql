CREATE TYPE DROP_POINT_STATUS AS ENUM ('FULL', 'EMPTY');
CREATE TABLE DROP_POINTS (
                           ID BIGINT AUTO_INCREMENT PRIMARY KEY,
                           LOCATION VARCHAR(255) NOT NULL,
                           CAPACITY INT NOT NULL,
                           CURRENT_EMPTIES_STOCK INT NOT NULL,
                           STATUS DROP_POINT_STATUS NOT NULL
);
