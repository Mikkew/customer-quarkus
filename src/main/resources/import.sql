-- Insertar 10 Clientes (Customer)
INSERT INTO Customer (id, code, accountNumber, names, surname, phone, address)
VALUES (1, 'CUST-001', 'ACC-1001', 'Juan', 'Pérez', '555-0101', 'Av. Reforma 123');

INSERT INTO Customer (id, code, accountNumber, names, surname, phone, address)
VALUES (2, 'CUST-002', 'ACC-1002', 'María', 'Gómez', '555-0102', 'Calle Insurgentes 45');

INSERT INTO Customer (id, code, accountNumber, names, surname, phone, address)
VALUES (3, 'CUST-003', 'ACC-1003', 'Carlos', 'López', '555-0103', 'Av. Juárez 789');

INSERT INTO Customer (id, code, accountNumber, names, surname, phone, address)
VALUES (4, 'CUST-004', 'ACC-1004', 'Ana', 'Martínez', '555-0104', 'Calle Hidalgo 12');

INSERT INTO Customer (id, code, accountNumber, names, surname, phone, address)
VALUES (5, 'CUST-005', 'ACC-1005', 'Luis', 'Hernández', '555-0105', 'Av. Morelos 456');

INSERT INTO Customer (id, code, accountNumber, names, surname, phone, address)
VALUES (6, 'CUST-006', 'ACC-1006', 'Sofia', 'Mendoza', '555-0106', 'Calle Zaragoza 89');

INSERT INTO Customer (id, code, accountNumber, names, surname, phone, address)
VALUES (7, 'CUST-007', 'ACC-1007', 'Fernando', 'Herrera', '555-0107', 'Av. Universidad 300');

INSERT INTO Customer (id, code, accountNumber, names, surname, phone, address)
VALUES (8, 'CUST-008', 'ACC-1008', 'Elena', 'Torres', '555-0108', 'Calle Pino Suárez 67');

INSERT INTO Customer (id, code, accountNumber, names, surname, phone, address)
VALUES (9, 'CUST-009', 'ACC-1009', 'Roberto', 'Sánchez', '555-0109', 'Av. Revolución 999');

INSERT INTO Customer (id, code, accountNumber, names, surname, phone, address)
VALUES (10, 'CUST-010', 'ACC-1010', 'Patricia', 'Ramírez', '555-0110', 'Calle Allende 234');


-- Insertar relaciones en Product (FK customer -> ID de Customer, product -> ID del Producto externo)
-- Cumple con el UniqueConstraint (customer, product)
INSERT INTO Product (id, customer, product) VALUES (1, 1, 101);
INSERT INTO Product (id, customer, product) VALUES (2, 1, 102);
INSERT INTO Product (id, customer, product) VALUES (3, 2, 103);
INSERT INTO Product (id, customer, product) VALUES (4, 3, 101);
INSERT INTO Product (id, customer, product) VALUES (5, 3, 104);
INSERT INTO Product (id, customer, product) VALUES (6, 4, 105);
INSERT INTO Product (id, customer, product) VALUES (7, 5, 106);
INSERT INTO Product (id, customer, product) VALUES (8, 6, 107);
INSERT INTO Product (id, customer, product) VALUES (9, 7, 108);
INSERT INTO Product (id, customer, product) VALUES (10, 8, 109);

-- Avanzar los IDs autogenerados para no chocar con los insertados a mano
ALTER TABLE Customer ALTER COLUMN id RESTART WITH 11;
ALTER TABLE Product ALTER COLUMN id RESTART WITH 11;
