# Persistencia y repositorios — Actividad 02

La configuración, los scripts y los ejemplos vigentes están en [README.md](../README.md).

Se utiliza un único modelo en `com.ecocompra.tech.entity`, cinco repositorios JPA,
la base `ecocompra_tech` y variables `DB_USER` / `DB_PASSWORD`. La integración conserva
el modelo ER y las evidencias del avance previo y del PR de Diana.

Para bases existentes se ofrece una migración que añade las restricciones únicas
sin eliminar tablas ni registros. El arranque solo valida el esquema.

El CRUD de productos y clientes acepta los ejemplos del README. Para categorías,
ventas, consultas JPQL/Named Queries y perfiles de pruebas, consultar esa misma guía.
