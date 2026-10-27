# Persistencia y repositorios — Actividad 02

Este avance integra el modelo ER, el esquema MySQL y las cinco entidades del
paquete `com.ecocompra.tech.entity` con los repositorios JPA. Conserva los
controladores de productos y clientes aportados en el PR de Diana.

## Base de datos y ejecución local

1. Usar Java 21. Maven se ejecuta con el wrapper incluido.
2. En una base nueva, ejecutar `docs/sql/01-esquema.sql` en MySQL. El esquema
   utilizado es `ecocompra_tech`. Si ya existen las cinco tablas, no repetir
   su creación: el arranque valida sus columnas con `ddl-auto=validate`.
3. Configurar en la terminal de PowerShell las credenciales del usuario de
   MySQL que tenga acceso a esa base. No guardar contraseñas en el repositorio:

   ```powershell
   $env:DB_USER="tu_usuario_mysql"
   $env:DB_PASSWORD="tu_clave_mysql"
   .\mvnw.cmd spring-boot:run
   ```

4. Abrir `http://localhost:8080` y `/api/estado`. Este último informa la
   configuración del proyecto; no ejecuta una consulta de diagnóstico.

## Repositorios

`CategoriaRepository`, `ProductoRepository`, `ClienteRepository`,
`VentaRepository` y `DetalleVentaRepository` extienden `JpaRepository`
con su entidad y un identificador `Long`.

Existe una sola clase `Categoria`, en `entity/Categoria.java`. Los productos
cargan su categoría al consultar por ID o listar, para devolver JSON sin
depender de una sesión de Hibernate abierta durante la respuesta.

## Operaciones disponibles

| Método | Ruta | Operación |
| --- | --- | --- |
| GET / POST | `/api/productos` | Listar / crear productos |
| GET / PUT / DELETE | `/api/productos/{id}` | Consultar / actualizar / eliminar |
| GET / POST | `/api/clientes` | Listar / crear clientes |
| GET / PUT / DELETE | `/api/clientes/{id}` | Consultar / actualizar / eliminar |

Los repositorios de categorías, ventas y detalles están disponibles para los
siguientes puntos; todavía no se exponen controladores para esas entidades.

Antes de crear productos, debe existir una categoría. Para una prueba manual
en una base de desarrollo:

```sql
INSERT INTO categoria (nombre, descripcion)
VALUES ('Laptops', 'Equipos portátiles');
SELECT id_categoria, nombre FROM categoria;
```

Ejemplo de POST a `/api/productos` (sustituir `idCategoria` por el ID obtenido):

```json
{
  "nombre": "Laptop de prueba",
  "precio": 1200.00,
  "stock": 3,
  "categoria": { "idCategoria": 1 }
}
```

Ejemplo de POST a `/api/clientes`:

```json
{
  "nombre": "Cliente de prueba",
  "correo": "cliente@example.com",
  "telefono": "999999999"
}
```

No enviar IDs al crear: los genera la base de datos. PUT requiere los campos
obligatorios del recurso completo. Los errores de validación devuelven 400;
los recursos o categorías inexistentes, 404; y el borrado de clientes o
productos vinculados a ventas, 409, conservando el historial.

Los campos de sostenibilidad pueden ser `null` si no se conoce el dato.
Si se informa consumo, se requieren sus condiciones de medición. Si se
informa reparabilidad, consumo o vida útil, se requiere una fuente técnica.
Estas validaciones corresponden a las restricciones del esquema SQL.

## Pruebas

```powershell
.\mvnw.cmd clean verify
```

Las pruebas usan H2 en memoria en modo MySQL, configurado únicamente en
`src/test/resources/application.properties`. No necesitan credenciales de
MySQL ni se conectan a la base de desarrollo. Comprueban el arranque,
las operaciones HTTP de productos y clientes, la serialización de categorías,
las validaciones, los cinco repositorios y la protección del historial de ventas.

La suite H2 no comprueba el dialecto, los permisos ni todas las restricciones
del script real de MySQL. Antes del merge se debe confirmar también el
arranque local con `ecocompra_tech` y una consulta HTTP a productos/clientes.

Este avance no incorpora la rama `feature/actividad-02`; su integración
requiere una revisión independiente de paquetes, configuración y SQL.
