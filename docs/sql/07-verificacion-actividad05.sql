-- Lectura de los datos ficticios de la prueba de Actividad 05. Sin claves ni hashes.
SELECT id,username,rol,activo,token_version FROM usuario WHERE username='practica05';
SELECT username, CASE WHEN password_hash LIKE '$2%' THEN 'BCrypt' ELSE 'REVISAR' END AS proteccion
FROM usuario WHERE username='practica05';
SELECT id_cliente,nombre,correo,telefono FROM cliente WHERE correo='practica05@example.com';
SELECT id_producto,nombre,precio,stock FROM producto WHERE nombre='Teclado Practica 05';
SELECT v.id_venta,v.id_cliente,d.id_producto,d.cantidad,d.precio_unitario
FROM venta v JOIN detalle_venta d ON d.id_venta=v.id_venta
JOIN cliente c ON c.id_cliente=v.id_cliente WHERE c.correo='practica05@example.com';
SELECT COUNT(*) AS ventas_con_cantidad_rechazada FROM venta v
JOIN detalle_venta d ON d.id_venta=v.id_venta JOIN cliente c ON c.id_cliente=v.id_cliente
WHERE c.correo='practica05@example.com' AND d.cantidad=100;
