INSERT INTO DROP_POINTS (LOCATION, CAPACITY, CURRENT_EMPTIES_STOCK, STATUS)
VALUES ('ErdGeschoss', 100, 90, 'FULL'),
       ('3 Stock', 80, 80, 'FULL_AND_NOTIFIED_TO_WAREHOUSE'),
       ('4 Stock', 120, 90, 'FULL'),
       ('6 Stock', 150, 150, 'FULL'),
       ('7 Stock', 60, 20, 'FULL'),
       ('8 Stock', 90, 85, 'EMPTY'),
       ('5 Stock', 70, 30, 'EMPTY'),
       ('2 Stock', 50, 50, 'FULL_AND_NOTIFIED_TO_WAREHOUSE'),
       ('9 Stock', 100, 95, 'EMPTY'),
       ('10 Stock', 110, 45, 'FULL');
ALTER TABLE "DROP_POINTS" ALTER COLUMN ID RESTART WITH (SELECT MAX(ID) + 1 FROM "DROP_POINTS");