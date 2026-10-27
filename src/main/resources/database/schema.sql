-- ========================================================
-- EcoCompra Tech - Sistema Web de Compras Tecnologicas Sostenibles
-- Script DDL: Creacion de Base de Datos y Tablas
-- Base de Datos: MySQL 8.x
-- ========================================================

CREATE DATABASE IF NOT EXISTS ecocompra_db
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE ecocompra_db;

-- 1. Desactivar temporalmente revision de claves foraneas para recreacion limpia
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS detalle_venta;
DROP TABLE IF EXISTS venta;
DROP TABLE IF EXISTS producto;
DROP TABLE IF EXISTS categoria;
DROP TABLE IF EXISTS cliente;

SET FOREIGN_KEY_CHECKS = 1;

-- ========================================================
-- 1. Tabla: categoria
-- ========================================================
CREATE TABLE categoria (
    id_categoria BIGINT AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500) NULL,
    CONSTRAINT pk_categoria PRIMARY KEY (id_categoria),
    CONSTRAINT uk_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================
-- 2. Tabla: producto
-- ========================================================
CREATE TABLE producto (
    id_producto BIGINT AUTO_INCREMENT,
    id_categoria BIGINT NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500) NULL,
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    reparabilidad VARCHAR(500) NULL,
    consumo_kwh DECIMAL(12,4) NULL,
    condiciones_consumo VARCHAR(500) NULL,
    vida_util_anios DECIMAL(5,2) NULL,
    fuente_tecnica VARCHAR(1000) NULL,
    CONSTRAINT pk_producto PRIMARY KEY (id_producto),
    CONSTRAINT fk_producto_categoria FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_producto_precio CHECK (precio >= 0),
    CONSTRAINT chk_producto_stock CHECK (stock >= 0),
    CONSTRAINT chk_producto_consumo CHECK (consumo_kwh IS NULL OR consumo_kwh >= 0),
    CONSTRAINT chk_producto_vida_util CHECK (vida_util_anios IS NULL OR vida_util_anios > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================
-- 3. Tabla: cliente
-- ========================================================
CREATE TABLE cliente (
    id_cliente BIGINT AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(150) NULL,
    telefono VARCHAR(20) NULL,
    CONSTRAINT pk_cliente PRIMARY KEY (id_cliente),
    CONSTRAINT uk_cliente_correo UNIQUE (correo)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================
-- 4. Tabla: venta
-- ========================================================
CREATE TABLE venta (
    id_venta BIGINT AUTO_INCREMENT,
    id_cliente BIGINT NOT NULL,
    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_venta PRIMARY KEY (id_venta),
    CONSTRAINT fk_venta_cliente FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente)
        ON DELETE RESTRICT
        ON UPDATE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- ========================================================
-- 5. Tabla: detalle_venta
-- ========================================================
CREATE TABLE detalle_venta (
    id_detalle BIGINT AUTO_INCREMENT,
    id_venta BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    CONSTRAINT pk_detalle_venta PRIMARY KEY (id_detalle),
    CONSTRAINT fk_detalle_venta FOREIGN KEY (id_venta)
        REFERENCES venta (id_venta)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT fk_detalle_producto FOREIGN KEY (id_producto)
        REFERENCES producto (id_producto)
        ON DELETE RESTRICT
        ON UPDATE CASCADE,
    CONSTRAINT chk_detalle_cantidad CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio_unitario CHECK (precio_unitario >= 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
