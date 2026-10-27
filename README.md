# EcoCompra Tech — Actividad 02

Proyecto de Spring Boot, Java 21, JPA/Hibernate y MySQL para registrar productos,
clientes y ventas, y consultar criterios de reparabilidad, consumo energético y vida útil.

Esta rama integra el modelo ER, SQL, evidencias, entidades y repositorios del
avance previo y del PR de Diana con las capas de servicios, DTO y consultas de
la Actividad 02. Existe un único paquete de entidades: `com.ecocompra.tech.entity`.

## Alcance de la entrega

| Punto | Implementación y evidencia |
| --- | --- |
| 4 | Modelo ER en `docs/diagramas/modelo-er-ecocompra.drawio.png` |
| 5 | Esquema `docs/sql/01-esquema.sql`; conexión a `ecocompra_tech` |
| 6 | Cinco entidades en `src/main/java/com/ecocompra/tech/entity` |
| 7 | Cinco interfaces `JpaRepository` en `repository` |
| 8 | CRUD de productos/clientes mediante controladores REST, servicios y DTO |
| 9 | JPQL con `@Query`, Named Queries en entidades y parámetros `@Param` |
| 10 | Pruebas de servicios, controladores y solicitudes HTTP; perfiles H2/MySQL |

La planificación y el informe académico deben conservarse también en la entrega;
esta tabla identifica las evidencias técnicas presentes en el repositorio.

## Organización

- `controller`: endpoints REST.
- `dto`: solicitudes y validaciones de entrada.
- `entity`: Categoria, Producto, Cliente, Venta y DetalleVenta; relaciones JPA y Named Queries.
- `repository`: acceso a datos y consultas parametrizadas.
- `service` / `service/impl`: reglas de negocio y transacciones.
- `exception`: errores HTTP centralizados.
- `docs/sql`: esquema, migración y datos opcionales de demostración.
- `src/test`: pruebas y configuración aislada.

## Configuración y ejecución en Windows

Requisito: Java 21 y MySQL 8.0.16 o superior (restricciones CHECK activas).
Utilizar el Maven Wrapper incluido; no es necesario instalar Maven por separado.

1. **Base nueva:** ejecutar `docs/sql/01-esquema.sql` con un usuario autorizado
   para crear la base y las tablas.
2. **Base existente del avance previo:** no recrear tablas. Revisar primero las
   consultas de duplicados de `docs/sql/02-integracion-actividad02.sql`. Si no
   devuelven filas, ejecutar sus dos ALTER TABLE una sola vez. Si hay duplicados,
   resolverlos conservando registros y relaciones antes de aplicar restricciones.
   No ejecutar esta migración en una base creada con el esquema nuevo: ya incluye
   los índices únicos. Respaldar la base antes de cualquier migración.
3. Crear o utilizar un usuario MySQL con SELECT, INSERT, UPDATE y DELETE sobre
   `ecocompra_tech`. La aplicación no requiere permisos para crear bases o tablas.
4. En PowerShell, dentro de la carpeta del proyecto:

   ```powershell
   $env:DB_USER="tu_usuario_mysql"
   $env:DB_PASSWORD="tu_clave_mysql"
   .\mvnw.cmd spring-boot:run
   ```

5. Abrir `http://localhost:8080`, `/api/estado`, `/api/productos` y `/api/clientes`.
   Sin registros, las consultas de listados devuelven `[]`.

La URL configurada es `jdbc:mysql://localhost:3306/ecocompra_tech`. Ajustar host
 o puerto en `application.properties` si el MySQL local utiliza otros valores.
Las credenciales se leen de variables de entorno, sin valores root predeterminados.
Hibernate utiliza `ddl-auto=validate`; no crea ni modifica las tablas. Los scripts
SQL no se ejecutan automáticamente (`spring.sql.init.mode=never`).

`docs/sql/03-datos-demostracion.sql` es opcional y debe ejecutarse solo en una base
**de pruebas vacía**, después del esquema. Contiene nombres, métricas y referencias
ficticios; no acredita reparabilidad, certificaciones ni ahorro energético reales.

## Validaciones y protección de datos

- Nombres obligatorios, tamaños máximos y correo con formato válido.
- Precios y stock no negativos; precisión decimal compatible con las columnas SQL.
- Consumo acompañado de condiciones de medición; métricas sostenibles acompañadas
  de fuente técnica. La aplicación exige la referencia, pero no verifica su autenticidad.
- Correos de clientes y nombres de categorías únicos.
- Clientes y productos relacionados con ventas no pueden eliminarse; categorías
  relacionadas con productos tampoco. Las relaciones no propagan borrados históricos.
- Parámetros enlazados en JPQL/Named Queries; no se forma la consulta concatenando entradas.
- HTTP 400 para JSON, parámetros o campos inválidos; 404 para recursos inexistentes;
  409 para conflictos de negocio/integridad; 405/415 para método/contenido no admitidos.
- No se serializan colecciones inversas ni detalles internos de excepciones inesperadas.

## 🚀 Catálogo de Endpoints de la API REST

### 1. Módulo Productos (`/api/productos`)
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/productos` | Lista todos los productos con su categoría |
| `GET` | `/api/productos/{id}` | Obtiene el detalle de un producto por ID |
| `POST` | `/api/productos` | Registra un nuevo producto (valida reglas sostenibles) |
| `PUT` | `/api/productos/{id}` | Actualiza un producto existente |
| `DELETE` | `/api/productos/{id}` | Elimina un producto (rechazado si tiene ventas) |
| `GET` | `/api/productos/buscar/nombre?nombre={txt}` | **JPQL:** Búsqueda por coincidencia de nombre |
| `GET` | `/api/productos/buscar/consumo?max={kwh}` | **JPQL:** Filtra equipos por consumo menor o igual |
| `GET` | `/api/productos/buscar/categoria/{id}` | **JPQL:** Lista productos de una categoría ordenados por precio |
| `GET` | `/api/productos/buscar/vida-util?min={anios}` | **JPQL:** Filtra equipos con vida útil mínima |
| `GET` | `/api/productos/buscar/precio?min={min}&max={max}` | **Named Query:** Búsqueda por rango de precio |
| `GET` | `/api/productos/buscar/categoria-stock?idCategoria={id}&stockMin={stock}` | **Named Query:** Filtro por categoría y stock mínimo |
| `GET` | `/api/productos/buscar/reparabilidad?criterio={txt}` | **Named Query:** Búsqueda por facilidad de reparación |

#### Ejemplo: registrar un producto de demostración

La categoría debe existir. Las métricas siguientes son ficticias, no evidencia técnica.

```json
{
  "idCategoria": 1,
  "nombre": "Equipo de demostración",
  "precio": 1200.00,
  "stock": 3
}
```

También se acepta `"categoria": {"idCategoria": 1}` por compatibilidad con el
avance de Diana. Si se envían ambos formatos, los identificadores deben coincidir.
No enviar `idProducto` ni `idCliente` al crear. PUT reemplaza los campos editables
completos; si se omite `stock`, se toma cero. Para conservarlo, enviarlo explícitamente.


---

### 2. Módulo Clientes (`/api/clientes`)
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/clientes` | Lista todos los clientes |
| `GET` | `/api/clientes/{id}` | Obtiene un cliente por su identificador |
| `POST` | `/api/clientes` | Registra un nuevo cliente (valida formato y unicidad de correo) |
| `PUT` | `/api/clientes/{id}` | Actualiza los datos de un cliente |
| `DELETE` | `/api/clientes/{id}` | Elimina un cliente (rechazado si registra ventas) |
| `GET` | `/api/clientes/buscar/telefono?telefono={tel}` | **JPQL:** Búsqueda por número telefónico |
| `GET` | `/api/clientes/buscar/dominio?dominio={dom}` | **JPQL:** Búsqueda por dominio de correo (ej. `.edu.pe`) |
| `GET` | `/api/clientes/buscar/correo?correo={email}` | **Named Query:** Búsqueda exacta por correo |
| `GET` | `/api/clientes/buscar/nombre?termino={txt}` | **Named Query:** Búsqueda por coincidencia de nombre |

---

### 3. Módulo Ventas y Demostración de Relaciones (`/api/ventas`)
| Método | Endpoint | Descripción |
| :--- | :--- | :--- |
| `GET` | `/api/ventas` | Lista todas las ventas con cabecera y líneas de detalle |
| `GET` | `/api/ventas/{id}` | Obtiene una venta completa por su ID |
| `POST` | `/api/ventas` | Registra una venta, descuenta stock y persiste detalles |
| `GET` | `/api/ventas/cliente/{idCliente}` | Consulta historial de ventas de un cliente específico |

#### Ejemplo de Petición JSON (`POST /api/ventas`):
```json
{
  "idCliente": 1,
  "detalles": [
    {
      "idProducto": 1,
      "cantidad": 2,
      "precioUnitario": 3499.00
    },
    {
      "idProducto": 3,
      "cantidad": 1,
      "precioUnitario": 1450.00
    }
  ]
}
```

---

## Pruebas y validación

Suite integrada: **61 pruebas**; combina las pruebas de la Actividad 02 con las del
avance de Diana y regresiones HTTP. Comprueba CRUD, búsquedas JPQL/Named Queries,
validaciones, serialización, errores HTTP, precisión decimal, IDs en POST, entradas
con comillas y protección del historial de ventas. No equivale a cobertura total
ni a una auditoría completa de seguridad.

### Pruebas aisladas con H2 (predeterminado)

```powershell
.\mvnw.cmd clean verify
```

No requieren DB_USER/DB_PASSWORD y no utilizan el MySQL de desarrollo. H2 funciona
 en memoria en modo MySQL con tablas creadas/eliminadas únicamente durante pruebas.

### Pruebas opcionales con MySQL real

Usar una instancia/base de pruebas **aislada y descartable**, con las cinco tablas
creadas mediante `docs/sql/01-esquema.sql`. No apuntar a datos de desarrollo o
producción: las pruebas preparan y eliminan registros. El usuario de prueba solo
necesita permisos DML; Hibernate valida el esquema existente sin modificarlo.

```powershell
$env:TEST_MYSQL_URL="jdbc:mysql://localhost:3306/ecocompra_tech?sslMode=REQUIRED&serverTimezone=UTC"
$env:TEST_MYSQL_USER="tu_usuario_de_pruebas"
$env:TEST_MYSQL_PASSWORD="tu_clave_de_pruebas"
.\mvnw.cmd "-Dspring.profiles.active=mysql" clean verify
```

`sslMode=REQUIRED` exige TLS. Ajustarlo solo según la configuración de la instancia
 de pruebas. La configuración de producción no incorpora credenciales de ejemplo.

La suite pasó con **H2 y MySQL Community Server 8.4.11**. También se comprobó con
MySQL que la migración conserva registros y que los CHECK rechazan consumo sin
condiciones y métricas sin fuente. Esto no prueba la conexión de cada integrante.

GitHub Actions (`.github/workflows/validacion.yml`) ejecuta H2 y MySQL 8.4 en cada
PR y en los pushes de las ramas configuradas. Las claves visibles allí pertenecen
solo a una base efímera de CI, sin acceso a bases externas.

Resultados y alcance: `docs/validacion-actividad02.md`.
