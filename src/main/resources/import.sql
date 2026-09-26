-- Insertar datos de prueba en la tabla customer
INSERT INTO customer (id, nombre, apellido, email, telefono)
VALUES (1, 'Juan', 'Pérez', 'juan.perez@example.com', '555-0101');

INSERT INTO customer (id, nombre, apellido, email, telefono)
VALUES (2, 'María', 'Gómez', 'maria.gomez@example.com', '555-0102');

INSERT INTO customer (id, nombre, apellido, email, telefono)
VALUES (3, 'Carlos', 'López', 'carlos.lopez@example.com', '555-0103');

INSERT INTO customer (id, nombre, apellido, email, telefono)
VALUES (4, 'Ana', 'Martínez', 'ana.martinez@example.com', '555-0104');

-- Reiniciar la secuencia de IDs para que las nuevas inserciones vía POST no colisionen (para bases de datos como H2 o PostgreSQL)
ALTER TABLE customer ALTER COLUMN id RESTART WITH 5;


-- Clientes
INSERT INTO customer (id, nombre, apellido, email, telefono) VALUES (1, 'Juan', 'Pérez', 'juan.perez@example.com', '555-0101');
INSERT INTO customer (id, nombre, apellido, email, telefono) VALUES (2, 'María', 'Gómez', 'maria.gomez@example.com', '555-0102');

-- Relación de IDs de productos por cliente
INSERT INTO Customer_productIds (Customer_id, productIds) VALUES (1, 1);
INSERT INTO Customer_productIds (Customer_id, productIds) VALUES (1, 3);
INSERT INTO Customer_productIds (Customer_id, productIds) VALUES (2, 2);

ALTER TABLE customer ALTER COLUMN id RESTART WITH 10;