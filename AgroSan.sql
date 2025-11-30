USE master;
GO

IF EXISTS (SELECT name FROM sys.databases WHERE name = N'AgroSanDB')
BEGIN
    ALTER DATABASE AgroSanDB SET SINGLE_USER WITH ROLLBACK IMMEDIATE;
    DROP DATABASE AgroSanDB;
END
GO

CREATE DATABASE AgroSanDB;
GO

USE AgroSanDB;
GO

CREATE TABLE ESTADO_PARCELA (
    id_estado_parcela INT IDENTITY(1,1) PRIMARY KEY,
    descripcion NVARCHAR(50) NOT NULL
);

CREATE TABLE PARCELA (
    id_parcela INT IDENTITY(1,1) PRIMARY KEY,
    ubicacion NVARCHAR(50) NOT NULL,
    area DECIMAL(10,2) NOT NULL,
    id_estado_parcela INT NOT NULL,
    FOREIGN KEY (id_estado_parcela) REFERENCES ESTADO_PARCELA(id_estado_parcela)
);

CREATE TABLE ESTADO_EMPLEADO (
    id_estado_empleado INT IDENTITY(1,1) PRIMARY KEY,
    descripcion NVARCHAR(50) NOT NULL
);

CREATE TABLE ROL_EMPLEADO (
    id_rol_empleado INT IDENTITY(1,1) PRIMARY KEY,
    descripcion NVARCHAR(50) NOT NULL
);

CREATE TABLE EMPLEADO (
    id_empleado INT IDENTITY(1,1) PRIMARY KEY,
    nombre NVARCHAR(50) NOT NULL,
    apellido NVARCHAR(50) NOT NULL,
    telefono NVARCHAR(20),
    email NVARCHAR(100),
    dni NVARCHAR(15),
    contraseña NVARCHAR(100),
    id_estado_empleado INT NOT NULL,
    id_rol_empleado INT NOT NULL,
    FOREIGN KEY (id_estado_empleado) REFERENCES ESTADO_EMPLEADO(id_estado_empleado),
    FOREIGN KEY (id_rol_empleado) REFERENCES ROL_EMPLEADO(id_rol_empleado)
);

CREATE TABLE ESTADO_ACTIVIDAD (
    id_estado_actividad INT IDENTITY(1,1) PRIMARY KEY,
    descripcion NVARCHAR(50) NOT NULL
);

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

CREATE TABLE TIPO_CULTIVO (
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

CREATE TABLE STOCK_COSECHA (
    id_stock_cosecha INT IDENTITY(1,1) PRIMARY KEY,
    id_tipo_cultivo INT NOT NULL,
    cantidad_disponible DECIMAL(10,2) NOT NULL,
    fecha_actualizacion DATE NOT NULL,
    FOREIGN KEY (id_tipo_cultivo) REFERENCES TIPO_CULTIVO(id_tipo_cultivo)
);

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