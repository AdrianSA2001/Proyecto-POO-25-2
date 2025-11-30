USE AgroSanDB;
GO

-- ===========================
-- 1. Insertar datos en tablas de ESTADOS
-- ===========================
INSERT INTO ESTADO_EMPLEADO (descripcion) VALUES 
('Activo'),
('De vacaciones'),
('Retirado'),
('Despedido');

INSERT INTO ESTADO_PARCELA (descripcion) VALUES
('Inactiva'),
('Activa');

INSERT INTO ESTADO_ACTIVIDAD (descripcion) VALUES
('Pendiente'),
('En curso'),
('Finalizada');

INSERT INTO ROL_EMPLEADO (descripcion) VALUES
('Jefe'),
('Vendedor'),
('Granjero');

-- ===========================
-- 2. Insertar datos en PARCELA
-- ===========================
INSERT INTO PARCELA (ubicacion, area, id_estado_parcela) VALUES
('Zona A', 120.0, 2),
('Zona A', 150.0, 2),
('Zona B', 100.0, 2),
('Zona C', 150.0, 1),
('Zona C', 120.0, 1),
('Zona A', 180.0, 1);

-- ===========================
-- 3. Insertar datos en EMPLEADO
-- ===========================
INSERT INTO EMPLEADO (nombre, apellido, telefono, email, dni, contraseña, id_estado_empleado, id_rol_empleado) VALUES
('Camila', 'Rivera', '998877665', 'cami2002@gmail.com', '77331122', 'pass0001', 1, 1),
('Rafaella', 'Loayza', '987654321', 'rafaellatkm01@gmail.com', '75318642', 'pass0002', 1, 2),
('Diego', 'Lulo', '991456230', 'diegopaladin2025@gmail.com', '89820661', 'pass0003', 3, 2),
('Luis', 'Barrantes', '920135006', 'barraluisasd@gmail.com', '65721143', 'pass0004', 4, 2),
('Sebastian', 'Paredes', '999666321', 'bastianredes1998@gmail.com', '63314577', 'pass0005', 1, 3);

-- ===========================
-- 4. Insertar datos en ACTIVIDAD
-- ===========================
INSERT INTO ACTIVIDAD (nombre, descripcion) VALUES
('Siembra', 'Inicio del cultivo'),
('Mantenimiento', 'Cuidado intermedio (reigo, abonado, control)'),
('Cosecha', 'Fin del ciclo de cultivo');

-- ===========================
-- 5. Insertar datos en TIPO_CULTIVO
-- ===========================
INSERT INTO TIPO_CULTIVO (nombre, tipo) VALUES
('Tomate', 'Fruta'),
('Alcachofa', 'Hortaliza'),
('Arandano', 'Fruta'),
('Fresa', 'Fruta'),
('Choclo', 'Cereal');

-- ===========================
-- 6. Insertar datos en STOCK_SEMILLAS
-- ===========================
INSERT INTO STOCK_SEMILLAS (id_tipo_cultivo, cantidad_disponible, fecha_actualizacion) VALUES
(1, 150.0, '2025-10-01'),
(2, 200.0, '2025-10-05'),
(3, 100.0, '2025-10-10'),
(4, 120.0, '2025-10-15'),
(5, 180.0, '2025-10-18');

-- ===========================
-- 7. Insertar datos en STOCK_COSECHA
-- ===========================
INSERT INTO STOCK_COSECHA (id_tipo_cultivo, cantidad_disponible, fecha_actualizacion) VALUES
(1, 150.0, '2025-10-30'),
(2, 45.0, '2025-10-30'),
(3, 120.0, '2025-10-28'),
(4, 0.0, '2025-10-01'),
(5, 0.0, '2025-10-01');

-- ===========================
-- 8. Insertar datos en HISTORIAL_SIEMBRA
-- ===========================
INSERT INTO HISTORIAL_SIEMBRA (id_tipo_cultivo, id_parcela, id_empleado, fecha_siembra, cantidad_sembrada) VALUES
(1, 1, 1, '2025-08-12', 40.0),
(2, 2, 2, '2025-08-12', 40.0),
(3, 3, 1, '2025-09-14', 60.0);

-- ===========================
-- 9. Insertar datos en HISTORIAL_COSECHA
-- ===========================
INSERT INTO HISTORIAL_COSECHA (id_tipo_cultivo, id_parcela, id_empleado, fecha_cosecha, cantidad_cosechada) VALUES
(1, 1, 1, '2025-09-15', 80.0),
(2, 2, 2, '2025-09-18', 40.0),
(1, 1, 1, '2025-10-22', 100.0),
(2, 2, 2, '2025-10-25', 35.0),
(3, 3, 5, '2025-10-28', 120.0);

-- ===========================
-- 11. Insertar datos en ACTIVIDAD_PROGRAMADA
-- ===========================
INSERT INTO ACTIVIDAD_PROGRAMADA (id_actividad, id_parcela, id_empleado, fecha_programada, id_estado_actividad) VALUES
(1, 1, 1, '2025-08-12', 3),
(1, 2, 2, '2025-08-12', 3),
(1, 3, 1, '2025-09-14', 3),
(3, 1, 1, '2025-09-15', 3),
(3, 2, 2, '2025-09-18', 3),
(3, 1, 1, '2025-10-22', 3),
(3, 2, 2, '2025-10-25', 3),
(3, 3, 5, '2025-10-28', 3);

-- ===========================
-- 13. Insertar datos en COMPRADOR y VENTA
-- ===========================
INSERT INTO COMPRADOR (nombre, telefono, email, direccion) VALUES
('Agroexport S.A.', '999555111', 'ventas@agroexport.com', 'Av. Agraria 123'),
('Mercado Central', '988444222', 'contacto@mercadocentral.com', 'Jr. Comercio 456');

INSERT INTO VENTA (id_comprador, id_tipo_cultivo, fecha_venta, cantidad_vendida, precio_unitario) VALUES
(1, 1, '2025-10-30', 30.0, 3.50),
(2, 2, '2025-10-30', 30.0, 3.00);
GO