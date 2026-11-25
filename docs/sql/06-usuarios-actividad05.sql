-- Solo para una base MySQL existente de A03/A04. Ejecutar una vez con permiso DDL.
-- Para una base nueva, 01-esquema.sql ya incluye la columna.
USE ecocompra_tech;
ALTER TABLE usuario ADD COLUMN token_version BIGINT NOT NULL DEFAULT 0;
-- Tras actualizar el codigo todos los usuarios deben iniciar sesion de nuevo.
