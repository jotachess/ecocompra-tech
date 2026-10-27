-- SOLO DEMOSTRACION: productos, clientes, métricas y fuentes ficticios.
-- Ejecutar únicamente en una base de pruebas vacía, después del esquema.
-- No usar estas cifras ni descripciones como evidencia de sostenibilidad.
-- ========================================================
-- EcoCompra Tech - Script DML: Datos iniciales de prueba
-- ========================================================

USE ecocompra_tech;

-- 1. Insertar categorias
INSERT INTO categoria (id_categoria, nombre, descripcion) VALUES
(1, 'Laptops Eco-eficientes', 'Equipos portatiles de bajo consumo energetico con piezas intercambiables'),
(2, 'Monitores Sostenibles', 'Pantallas LED de alta eficiencia con certificacion Energy Star'),
(3, 'Servidores de Bajo Consumo', 'Servidores rack optimizados para refrigeracion pasiva y menor huella de carbono'),
(4, 'Accesorios Reciclados', 'Teclados, ratones y perifericos elaborados con plasticos recuperados');

-- 2. Insertar productos
INSERT INTO producto (id_producto, id_categoria, nombre, descripcion, precio, stock, reparabilidad, consumo_kwh, condiciones_consumo, vida_util_anios, fuente_tecnica) VALUES
(1, 1, 'Laptop VerdeBook Pro 14', 'Chasis de aluminio reciclado y modular para facil actualizacion', 3499.00, 15, 'Ejemplo ficticio de RAM y SSD reemplazables', 0.0450, '8 horas diarias navegacion y ofimatica a 50% brillo', 6.00, 'DEMO: ficha ficticia, no valida una decisión de compra'),
(2, 1, 'Ultrabook EcoAir 13', 'Procesador ultra eficiente sin ventilador activo', 2899.50, 20, 'Bateria reemplazable con desarmador estandar', 0.0320, '6 horas continuas carga estandar oficina', 5.50, 'DEMO: ficha ficticia, no valida una decisión de compra'),
(3, 2, 'Monitor SolarView 27" 4K', 'Panel IPS con sensor de luz ambiental y apagado automatico', 1450.00, 25, 'Fuente de alimentacion externa de facil sustitucion', 0.0210, 'Uso brillo adaptativo estandar 8h/dia', 8.00, 'DEMO: ficha ficticia, no valida una decisión de compra'),
(4, 2, 'Monitor CleanScreen 24" FHD', 'Marco plastico 100% PCR con modo ahorro energetico', 780.00, 40, 'Manual de desensamble publico libre', 0.0150, 'Modo eco activado 8h/dia', 7.00, 'DEMO: ficha ficticia, no valida una decisión de compra'),
(5, 3, 'Servidor EcoServer Blade X2', 'Servidor nodo doble con eficiencia de fuente 80 PLUS Titanium', 9200.00, 5, 'Componentes hot-swap modulares', 0.3800, 'Carga 70% 24/7 en centro de datos climatizado', 10.00, 'DEMO: ficha ficticia, no valida una decisión de compra'),
(6, 4, 'Teclado Solar Bamboo Wireless', 'Teclado inalambrico con celda solar y carcasa de bambu organico', 189.90, 50, 'Interruptores mecanicos extraibles individualmente', NULL, NULL, 4.00, 'DEMO: ficha ficticia, no valida una decisión de compra');

-- 3. Insertar clientes
INSERT INTO cliente (id_cliente, nombre, correo, telefono) VALUES
(1, 'Empresa de Consultoria Ambiental S.A.C.', 'contacto@consultoriaverde.pe', '+51987654321'),
(2, 'Universidad Tecnologica de Innovacion', 'compras@uti.edu.pe', '+51912345678'),
(3, 'Cooperativa Agraria EcoValle', 'administracion@ecovalle.org', '+51945678123'),
(4, 'Carlos Enrique Ramirez Soto', 'carlos.ramirez@gmail.com', '+51965432198');

-- 4. Insertar ventas
INSERT INTO venta (id_venta, id_cliente, fecha) VALUES
(1, 1, '2026-10-20 10:30:00'),
(2, 2, '2026-10-22 15:45:00');

-- 5. Insertar detalle de ventas
INSERT INTO detalle_venta (id_detalle, id_venta, id_producto, cantidad, precio_unitario) VALUES
(1, 1, 1, 3, 3499.00),
(2, 1, 3, 3, 1450.00),
(3, 2, 2, 5, 2899.50),
(4, 2, 4, 5, 780.00);
