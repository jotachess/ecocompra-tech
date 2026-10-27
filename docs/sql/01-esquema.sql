CREATE DATABASE IF NOT EXISTS ecocompra_tech
    CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ecocompra_tech;

-- 1. Categorias
CREATE TABLE categoria (
    id_categoria BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    PRIMARY KEY (id_categoria),
    CONSTRAINT uk_categoria_nombre UNIQUE (nombre),
    CONSTRAINT chk_categoria_nombre
        CHECK (CHAR_LENGTH(TRIM(nombre)) > 0)
) ENGINE=InnoDB;

-- 2. Productos
CREATE TABLE producto (
    id_producto BIGINT NOT NULL AUTO_INCREMENT,
    id_categoria BIGINT NOT NULL,
    nombre VARCHAR(120) NOT NULL,
    descripcion VARCHAR(500),
    precio DECIMAL(10,2) NOT NULL,
    stock INT NOT NULL DEFAULT 0,
    reparabilidad VARCHAR(500),
    consumo_kwh DECIMAL(12,4),
    condiciones_consumo VARCHAR(500),
    vida_util_anios DECIMAL(5,2),
    fuente_tecnica VARCHAR(1000),
    PRIMARY KEY (id_producto),

    CONSTRAINT fk_producto_categoria
        FOREIGN KEY (id_categoria)
        REFERENCES categoria (id_categoria)
        ON DELETE RESTRICT,

    CONSTRAINT chk_producto_nombre
        CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),
    CONSTRAINT chk_producto_precio
        CHECK (precio >= 0),
    CONSTRAINT chk_producto_stock
        CHECK (stock >= 0),
    CONSTRAINT chk_producto_consumo
        CHECK (consumo_kwh IS NULL OR consumo_kwh >= 0),
    CONSTRAINT chk_producto_vida
        CHECK (vida_util_anios IS NULL OR vida_util_anios > 0),

    CONSTRAINT chk_producto_condiciones
        CHECK (
            consumo_kwh IS NULL
            OR (
                condiciones_consumo IS NOT NULL
                AND CHAR_LENGTH(TRIM(condiciones_consumo)) > 0
            )
        ),

    CONSTRAINT chk_producto_fuente
        CHECK (
            (
                reparabilidad IS NULL
                AND consumo_kwh IS NULL
                AND vida_util_anios IS NULL
            )
            OR (
                fuente_tecnica IS NOT NULL
                AND CHAR_LENGTH(TRIM(fuente_tecnica)) > 0
            )
        )
) ENGINE=InnoDB;

-- 3. Clientes
CREATE TABLE cliente (
    id_cliente BIGINT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(120) NOT NULL,
    correo VARCHAR(150),
    telefono VARCHAR(20),
    PRIMARY KEY (id_cliente),
    CONSTRAINT uk_cliente_correo UNIQUE (correo),
    CONSTRAINT chk_cliente_nombre
        CHECK (CHAR_LENGTH(TRIM(nombre)) > 0)
) ENGINE=InnoDB;

-- 4. Ventas
CREATE TABLE venta (
    id_venta BIGINT NOT NULL AUTO_INCREMENT,
    id_cliente BIGINT NOT NULL,
    fecha DATETIME NOT NULL,
    PRIMARY KEY (id_venta),

    CONSTRAINT fk_venta_cliente
        FOREIGN KEY (id_cliente)
        REFERENCES cliente (id_cliente)
        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- 5. Detalles de venta
CREATE TABLE detalle_venta (
    id_detalle BIGINT NOT NULL AUTO_INCREMENT,
    id_venta BIGINT NOT NULL,
    id_producto BIGINT NOT NULL,
    cantidad INT NOT NULL,
    precio_unitario DECIMAL(10,2) NOT NULL,
    PRIMARY KEY (id_detalle),

    CONSTRAINT fk_detalle_venta
        FOREIGN KEY (id_venta)
        REFERENCES venta (id_venta)
        ON DELETE RESTRICT,

    CONSTRAINT fk_detalle_producto
        FOREIGN KEY (id_producto)
        REFERENCES producto (id_producto)
        ON DELETE RESTRICT,

    CONSTRAINT chk_detalle_cantidad
        CHECK (cantidad > 0),
    CONSTRAINT chk_detalle_precio
        CHECK (precio_unitario >= 0)
) ENGINE=InnoDB;

-- Comprobar las tablas creadas
SHOW TABLES;