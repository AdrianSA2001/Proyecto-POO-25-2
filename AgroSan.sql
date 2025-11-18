USE master;
GO

-- Si la base de datos ya existe, eliminarla
IF EXISTS (SELECT name FROM sys.databases WHERE name = N'AgroSanDB')
BEGIN
    ALTER DATABASE AgroSanDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE AgroSanDB;
END
GO

-- Crear la nueva base de datos
CREATE DATABASE AgroSanDB;
GO

USE AgroSanDB;
GO

-- ===========================
-- 1. Tablas de ESTADOS
-- ===========================
CREATE TABLE ESTADO_EMPLEADO (
    id_estado_empleado INT IDENTITY(1,1) PRIMARY KEY,
    descripcion NVARCHAR(50) NOT NULL
);

CREATE TABLE ESTADO_PARCELA (
    id_estado_parcela INT IDENTITY(1,1) PRIMARY KEY,
    descripcion NVARCHAR(50) NOT NULL
);

CREATE TABLE ESTADO_ACTIVIDAD (
    id_estado_actividad INT IDENTITY(1,1) PRIMARY KEY,
    descripcion NVARCHAR(50) NOT NULL
);

-- ===========================
-- 2. Tabla PARCELA
-- ===========================
CREATE TABLE PARCELA (
    id_parcela INT IDENTITY(1,1) PRIMARY KEY,
    ubicacion NVARCHAR(50) NOT NULL,
    area DECIMAL(10,2) NOT NULL,
    id_estado_parcela INT NOT NULL,
    FOREIGN KEY (id_estado_parcela) REFERENCES ESTADO_PARCELA(id_estado_parcela)
);

-- ===========================
-- 3. Tabla EMPLEADO
-- ===========================
CREATE TABLE EMPLEADO (
    id_empleado INT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(50) NOT NULL,
    apellido NVARCHAR(50) NOT NULL,
    telefono NVARCHAR(20),
    email NVARCHAR(100),
    dni NVARCHAR(15),
    contraseña NVARCHAR(100),
    id_estado_empleado INT NOT NULL,
    FOREIGN KEY (id_estado_empleado) REFERENCES ESTADO_EMPLEADO(id_estado_empleado)
);

-- ===========================
-- 4. Tablas de ACTIVIDADES
-- ===========================
CREATE TABLE ACTIVIDAD (
    id_actividad INT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(50) NOT NULL,
    descripcion NVARCHAR(255)
);

CREATE TABLE ACTIVIDAD_PROGRAMADA (
    id_actividad_programada INT IDENTITY(1,1) PRIMARY KEY,
    id_actividad INT NOT NULL,
    id_parcela INT NOT NULL,
    id_empleado INT NOT NULL,
    fecha_programada DATE NOT NULL,
    id_estado_actividad INT NOT NULL,
    FOREIGN KEY (id_actividad) REFERENCES ACTIVIDAD(id_actividad),
    FOREIGN KEY (id_parcela) REFERENCES PARCELA(id_parcela),
    FOREIGN KEY (id_empleado) REFERENCES EMPLEADO(id_empleado),
    FOREIGN KEY (id_estado_actividad) REFERENCES ESTADO_ACTIVIDAD(id_estado_actividad)
);

-- ===========================
-- 5. Tablas de CULTIVOS
-- ===========================
CREATE TABLE TIPO_CULTIVO    (
    id_tipo_cultivo INT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(50) NOT NULL,
    tipo NVARCHAR(50)
);

CREATE TABLE STOCK_SEMILLAS (
    id_stock_semilla INT IDENTITY(1,1) PRIMARY KEY,
    id_tipo_cultivo INT NOT NULL,
    cantidad_disponible DECIMAL(10,2) NOT NULL,
    fecha_actualizacion DATE NOT NULL,
    FOREIGN KEY (id_tipo_cultivo) REFERENCES TIPO_CULTIVO(id_tipo_cultivo)
);

CREATE TABLE STOCK_COSECHA (
    id_stock_cosecha INT IDENTITY(1,1) PRIMARY KEY,
    id_tipo_cultivo INT NOT NULL,
    cantidad_disponible DECIMAL(10,2) NOT NULL,
    fecha_actualizacion DATE NOT NULL,
    FOREIGN KEY (id_tipo_cultivo) REFERENCES TIPO_CULTIVO(id_tipo_cultivo)
);

-- ===========================
-- 6. HISTORIALES
-- ===========================
CREATE TABLE HISTORIAL_SIEMBRA (
    id_siembra INT IDENTITY(1,1) PRIMARY KEY,
    id_tipo_cultivo INT NOT NULL,
    id_parcela INT NOT NULL,
    id_empleado INT NOT NULL,
    fecha_siembra DATE NOT NULL,
    cantidad_sembrada DECIMAL(10,2),
    FOREIGN KEY (id_tipo_cultivo) REFERENCES TIPO_CULTIVO(id_tipo_cultivo),
    FOREIGN KEY (id_parcela) REFERENCES PARCELA(id_parcela),
    FOREIGN KEY (id_empleado) REFERENCES EMPLEADO(id_empleado)
);

CREATE TABLE HISTORIAL_COSECHA (
    id_cosecha INT IDENTITY(1,1) PRIMARY KEY,
    id_tipo_cultivo INT NOT NULL,
    id_parcela INT NOT NULL,
    id_empleado INT NOT NULL,
    fecha_cosecha DATE NOT NULL,
    cantidad_cosechada DECIMAL(10,2),
    FOREIGN KEY (id_tipo_cultivo) REFERENCES TIPO_CULTIVO(id_tipo_cultivo),
    FOREIGN KEY (id_parcela) REFERENCES PARCELA(id_parcela),
    FOREIGN KEY (id_empleado) REFERENCES EMPLEADO(id_empleado)
);

-- ===========================
-- 7. TABLAS DE INSUMOS
-- ===========================
CREATE TABLE INSUMO (
    id_insumo INT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(50) NOT NULL,
    descripcion NVARCHAR(255),
    unidad_medida NVARCHAR(20),
    stock_actual DECIMAL(10,2),
    stock_minimo DECIMAL(10,2)
);

CREATE TABLE USO_INSUMO (
    id_uso_insumo INT IDENTITY(1,1) PRIMARY KEY,
    id_actividad_programada INT NOT NULL,
    id_insumo INT NOT NULL,
    cantidad_usada DECIMAL(10,2),
    FOREIGN KEY (id_actividad_programada) REFERENCES ACTIVIDAD_PROGRAMADA(id_actividad_programada),
    FOREIGN KEY (id_insumo) REFERENCES INSUMO(id_insumo)
);

-- ===========================
-- 8. TABLAS DE VENTA
-- ===========================
CREATE TABLE COMPRADOR (
    id_comprador INT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(100) NOT NULL,
    telefono NVARCHAR(20),
    email NVARCHAR(100),
    direccion NVARCHAR(255)
);

CREATE TABLE VENTA (
    id_venta INT IDENTITY(1,1) PRIMARY KEY,
    id_comprador INT NOT NULL,
    id_tipo_cultivo INT NOT NULL,
    fecha_venta DATE NOT NULL,
    cantidad_vendida DECIMAL(10,2),
    precio_unitario DECIMAL(10,2),
    FOREIGN KEY (id_comprador) REFERENCES COMPRADOR(id_comprador),
    FOREIGN KEY (id_tipo_cultivo) REFERENCES TIPO_CULTIVO(id_tipo_cultivo)
);
GO