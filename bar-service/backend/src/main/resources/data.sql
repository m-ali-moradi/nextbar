-- === Insert BAR ===
INSERT INTO BAR (ID, NAME, LOCATION, MAX_CAPACITY)
VALUES ('11111111-1111-1111-1111-111111111111', 'BarCool', 'EF38B', '300');

-- === Insert PRODUCTS ===
INSERT INTO PRODUCT (ID, NAME, UNIT_TYPE)
VALUES ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Fanta', ''),
       ('cccccccc-cccc-cccc-cccc-cccccccccccc', 'Cola Zero', 'can'),
       ('dddddddd-dddd-dddd-dddd-dddddddddddd', 'Beer', 'bottle'),
       ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 'Red Bull', 'can');

-- === Insert BAR_STOCK_ITEM ===
INSERT INTO BAR_STOCK_ITEM (ID, BAR_ID, PRODUCT_ID, QUANTITY, UPDATED_AT)
VALUES (RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 20,
        CURRENT_TIMESTAMP),
       (RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'cccccccc-cccc-cccc-cccc-cccccccccccc', 18,
        CURRENT_TIMESTAMP),
       (RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'dddddddd-dddd-dddd-dddd-dddddddddddd', 10,
        CURRENT_TIMESTAMP),
       (RANDOM_UUID(), '11111111-1111-1111-1111-111111111111', 'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', 19,
        CURRENT_TIMESTAMP);


