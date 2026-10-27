-- Migración manual para bases creadas con el esquema anterior a la actividad 02.
-- No ejecutar si ya se creó la base con la versión actual de 01-esquema.sql.
-- No elimina tablas ni registros. Ejecutar una sola vez, tras realizar respaldo.
USE ecocompra_tech;

-- Comprobar primero: ambas consultas deben devolver cero filas.
-- Si hay duplicados, resolverlos conservando los registros y sus relaciones.
SELECT nombre, COUNT(*) AS cantidad
FROM categoria GROUP BY nombre HAVING COUNT(*) > 1;
SELECT correo, COUNT(*) AS cantidad
FROM cliente WHERE correo IS NOT NULL GROUP BY correo HAVING COUNT(*) > 1;

ALTER TABLE categoria ADD CONSTRAINT uk_categoria_nombre UNIQUE (nombre);
ALTER TABLE cliente ADD CONSTRAINT uk_cliente_correo UNIQUE (correo);
