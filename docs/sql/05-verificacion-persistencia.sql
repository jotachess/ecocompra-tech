-- Consultas de lectura para la evidencia de la Actividad 04.
-- Ejecutar antes de retirar los registros ficticios de la prueba.
SELECT id_cliente, nombre, telefono
FROM cliente WHERE correo = 'practica04@example.com';
SELECT id_producto, nombre, precio, stock
FROM producto WHERE nombre = 'Teclado Practica 04';
SELECT v.id_venta, v.id_cliente, d.id_producto, d.cantidad, d.precio_unitario
FROM venta v JOIN detalle_venta d ON v.id_venta = d.id_venta
JOIN cliente c ON c.id_cliente = v.id_cliente
WHERE c.correo = 'practica04@example.com';
