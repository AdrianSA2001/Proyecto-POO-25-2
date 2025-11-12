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
('Registrada / No apta aún para siembra'),
('En preparación del suelo'),
('Lista para siembra'),
('Sembrada / Cultivo activo'),
('En mantenimiento del cultivo'),
('En descanso / Sin cultivo'),
('Inutilizable / Terreno dañado');

INSERT INTO ESTADO_ACTIVIDAD (descripcion) VALUES
('Pendiente'),
('En curso'),
('Finalizada');

-- ===========================
-- 2. Insertar datos en PARCELA
-- ===========================
INSERT INTO PARCELA (nombre, area, id_estado_parcela) VALUES
('Zona A', 100.0, 3),
('Zona B', 100.0, 3),
('Zona A', 120.0, 3),
('Zona C', 120.0, 3),
('Zona D', 150.0, 3);

-- ===========================
-- 3. Insertar datos en EMPLEADO
-- ===========================
INSERT INTO EMPLEADO (nombre, apellido, telefono, email, dni, contraseña, id_estado_empleado) VALUES
('Juan', 'Pérez', '999888777', 'juanperez@example.com', '12345678', '1234', 1),
('María', 'López', '987654321', 'marialopez@example.com', '87654321', 'abcd', 1),
('Carlos', 'Gómez', '998877665', 'carlosgomez@example.com', '11223344', 'pass', 2);

-- ===========================
-- 4. Insertar datos en ACTIVIDAD
-- ===========================
INSERT INTO ACTIVIDAD (nombre, descripcion) VALUES
('Arado del terreno', 'Preparar la tierra para la siembra.'),
('Riego', 'Aplicar agua a los cultivos.'),
('Siembra', 'Colocar las semillas o plántulas.'),
('Abonado', 'Aplicar fertilizantes al suelo.'),
('Aplicación de insecticidas', 'Controlar plagas y enfermedades.'),
('Deshierbe', 'Eliminar malezas del cultivo.'),
('Mantenimiento', 'Revisar y cuidar el cultivo.'),
('Cosecha', 'Recolectar los productos maduros.');

-- ===========================
-- 5. Insertar datos en TIPO_CULTIVO
-- ===========================
INSERT INTO TIPO_CULTIVO (nombre, tipo) VALUES
('Tomate', 'Hortaliza'),
('Lechuga', 'Hortaliza'),
('Zanahoria', 'Hortaliza'),
('Fresa', 'Fruta'),
('Maíz Choclo', 'Cereal');

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
(1, 50.0, '2025-10-20'),
(2, 80.0, '2025-10-22'),
(3, 30.0, '2025-10-25'),
(4, 60.0, '2025-10-27'),
(5, 90.0, '2025-10-30');

-- ===========================
-- 8. Insertar datos en HISTORIAL_SIEMBRA
-- ===========================
INSERT INTO HISTORIAL_SIEMBRA (id_tipo_cultivo, id_parcela, id_empleado, fecha_siembra, cantidad_sembrada) VALUES
(1, 1, 1, '2025-09-10', 50.0),
(2, 2, 2, '2025-09-12', 60.0),
(3, 3, 1, '2025-09-15', 40.0),
(4, 4, 2, '2025-09-18', 55.0),
(5, 5, 3, '2025-09-20', 70.0);

-- ===========================
-- 9. Insertar datos en HISTORIAL_COSECHA
-- ===========================
INSERT INTO HISTORIAL_COSECHA (id_tipo_cultivo, id_parcela, id_empleado, fecha_cosecha, cantidad_cosechada) VALUES
(1, 1, 1, '2025-10-15', 30.0),
(2, 2, 2, '2025-10-18', 40.0),
(3, 3, 1, '2025-10-22', 25.0),
(4, 4, 2, '2025-10-25', 45.0),
(5, 5, 3, '2025-10-28', 60.0);

-- ===========================
-- 10. Insertar datos en INSUMO
-- ===========================
INSERT INTO INSUMO (nombre, descripcion, unidad_medida, stock_actual, stock_minimo) VALUES
('Fertilizante NPK', 'Fertilizante balanceado para cultivos', 'kg', 500, 100),
('Pesticida X', 'Control de plagas de hoja', 'L', 200, 50),
('Herbicida Y', 'Eliminación de malezas', 'L', 150, 30);

-- ===========================
-- 11. Insertar datos en ACTIVIDAD_PROGRAMADA
-- ===========================
INSERT INTO ACTIVIDAD_PROGRAMADA (id_actividad, id_parcela, id_empleado, fecha_programada, id_estado_actividad) VALUES
(1, 1, 1, '2025-09-05', 3),
(2, 2, 2, '2025-09-20', 3),
(3, 1, 1, '2025-10-15', 3);

-- ===========================
-- 12. Insertar datos en USO_INSUMO
-- ===========================
INSERT INTO USO_INSUMO (id_actividad_programada, id_insumo, cantidad_usada) VALUES
(1, 1, 10.5),
(2, 2, 3.0),
(3, 3, 5.0);

-- ===========================
-- 13. Insertar datos en COMPRADOR y VENTA
-- ===========================
INSERT INTO COMPRADOR (nombre, telefono, email, direccion) VALUES
('Agroexport S.A.', '999555111', 'ventas@agroexport.com', 'Av. Agraria 123'),
('Mercado Central', '988444222', 'contacto@mercadocentral.com', 'Jr. Comercio 456');

INSERT INTO VENTA (id_comprador, id_tipo_cultivo, fecha_venta, cantidad_vendida, precio_unitario) VALUES
(1, 1, '2025-10-20', 20.0, 3.50),
(2, 2, '2025-10-22', 30.0, 2.80);
GO
