# Validación de Actividad 02

Fecha: 27 de octubre de 2026.

Se integraron los aportes de `feature/actividad-02` y `feature/repositorios-diana`
en una copia aislada antes de publicar. Se conservaron el diagrama ER, el esquema,
las evidencias previas, los cinco repositorios y un solo conjunto de entidades
en `com.ecocompra.tech.entity`.

## Resultados reproducidos

| Comprobación | Resultado |
| --- | --- |
| Java 21, Maven Wrapper, `clean verify` con H2 | BUILD SUCCESS |
| Suite H2 | 61 ejecutadas, 0 fallos, 0 errores, 0 omitidas |
| Suite con MySQL Community Server 8.4.11 y `ddl-auto=validate` | 61 ejecutadas, 0 fallos, 0 errores, 0 omitidas |
| Creación del esquema canónico en MySQL | Cinco tablas creadas y validadas por Hibernate |
| Usuario de aplicación de pruebas | SELECT, INSERT, UPDATE, DELETE; sin permisos para crear/modificar tablas |
| Migración de base previa a Actividad 02 | Conservó una categoría, un cliente y un producto existentes |
| CHECK de fuente técnica en MySQL | Rechazó inserción de reparabilidad sin fuente (error 3819) |
| CHECK de condiciones de consumo en MySQL | Rechazó inserción de consumo sin condiciones (error 3819) |

## Cobertura comprobada

- Registro, listado, búsqueda por ID, actualización y eliminación de clientes/productos.
- Consultas JPQL y Named Queries con parámetros; entradas con comillas conservadas como datos.
- JSON de productos con categoría, sin depender de una sesión abierta durante la respuesta.
- Formato de categoría plano (`idCategoria`) y formato anterior (`categoria.idCategoria`).
- Rechazo de IDs en POST, categorías contradictorias y categorías inexistentes.
- Nombres, correo, tamaños, precios, stock y límites de precisión decimal.
- Condiciones de medición y fuente técnica al informar datos de sostenibilidad.
- HTTP 400 para JSON mal formado, parámetros ausentes y tipos incorrectos.
- HTTP 404, 409, 405 y 415 para las situaciones correspondientes.
- Persistencia de las cinco entidades y protección del historial ante intentos de borrado.
- Registro transaccional de ventas y descuento de stock en los casos de la suite original.

Las tres regresiones HTTP que originalmente devolvían 500 ahora devuelven 400.
Las 31 pruebas originales se mantienen; la suite integrada añade las pruebas del
avance de Diana y las nuevas regresiones, hasta llegar a 61.

## Límites

Las ejecuciones usaron bases aisladas, no el MySQL de cada integrante. La suite
no demuestra cobertura total, seguridad completa, rendimiento ni corrección bajo
ventas concurrentes. Las fuentes técnicas exigidas por el formulario no se
verifican automáticamente y los datos de demostración son ficticios.

La validación automática de GitHub Actions se configura en
`.github/workflows/validacion.yml`; sus resultados deben revisarse en el PR.
El merge a `main` permanece como una acción separada.
