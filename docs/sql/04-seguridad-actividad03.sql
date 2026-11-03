-- Ejecutar sobre ecocompra_tech con un usuario autorizado para DDL.
-- No elimina ni modifica las tablas de la Actividad 02.
USE ecocompra_tech;
CREATE TABLE IF NOT EXISTS usuario (
    id BIGINT NOT NULL AUTO_INCREMENT,
    username VARCHAR(80) NOT NULL,
    password_hash VARCHAR(100) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,
    PRIMARY KEY (id),
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT chk_usuario_rol CHECK (rol IN ('ADMIN','VENDEDOR'))
) ENGINE=InnoDB;
-- Las cuentas iniciales se crean con BCrypt al iniciar el backend, mediante
-- ADMIN_PASSWORD y VENDEDOR_PASSWORD. No guardar contrasenas en este script.
