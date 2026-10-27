# EcoCompra Tech – Sistema Web de Compras Tecnológicas Sostenibles

Proyecto desarrollado con **Java 21** y **Spring Boot 4.1.2** para la evaluación de requerimientos de compras corporativas y proveedores bajo criterios de **reparabilidad, consumo energético y vida útil estimada**, alineado al ODS 9 (Industria, Innovación e Infraestructura).

---

## 📂 Arquitectura y Descripción de Carpetas del Proyecto

El proyecto está diseñado bajo una arquitectura limpia por capas (*Layered Architecture*), asegurando la separación de responsabilidades, alta cohesión y bajo acoplamiento:

```text
ecocompra-tech/
├── .mvn/                                      # Configuración y binarios del Maven Wrapper
├── mvnw / mvnw.cmd                            # Scripts ejecutables de Maven Wrapper para Unix y Windows
├── pom.xml                                    # Gestión de dependencias (Spring Boot 4.1.2, JPA, Validation, MySQL, H2)
├── README.md                                  # Documentación integral del proyecto y guía de uso
├── src/
│   ├── main/
│   │   ├── java/com/ecocompra/tech/
│   │   │   ├── EcocompraTechApplication.java  # Clase principal de arranque de Spring Boot
│   │   │   ├── controller/                    # Capa de presentación REST (Controladores HTTP)
│   │   │   │   ├── CategoriaController.java   # Endpoints CRUD para Categorías
│   │   │   │   ├── ClienteController.java     # Endpoints CRUD, JPQL y Named Queries de Clientes
│   │   │   │   ├── InicioController.java      # Endpoint de verificación de estado (/api/estado)
│   │   │   │   ├── ProductoController.java    # Endpoints CRUD, JPQL y Named Queries de Productos
│   │   │   │   └── VentaController.java       # Endpoints para registro y consulta de Ventas
│   │   │   ├── dto/                           # Data Transfer Objects y objetos de petición/respuesta
│   │   │   │   ├── CategoriaDto.java          # Criterios de validación para categorías
│   │   │   │   ├── ClienteDto.java            # Criterios de validación para clientes
│   │   │   │   ├── DetalleVentaRequestDto.java# Línea de detalle para registro de venta
│   │   │   │   ├── ErrorResponseDto.java      # Estructura estandarizada de errores HTTP
│   │   │   │   ├── ProductoDto.java           # DTO con reglas y métricas sostenibles
│   │   │   │   └── VentaRequestDto.java       # Solicitud de venta con validación de cabecera y detalles
│   │   │   ├── exception/                     # Gestión centralizada de excepciones
│   │   │   │   ├── GlobalExceptionHandler.java# @RestControllerAdvice para códigos HTTP 400, 404, 409, 500
│   │   │   │   ├── IllegalOperationException.java # Excepción por violación de reglas o integridad referencial
│   │   │   │   └── ResourceNotFoundException.java # Excepción lanzada cuando no existe un recurso por ID
│   │   │   ├── model/                         # Capa de dominio y persistencia (Entidades JPA)
│   │   │   │   ├── Categoria.java             # Entidad JPA para la tabla 'categoria'
│   │   │   │   ├── Cliente.java               # Entidad JPA para 'cliente' con Named Queries
│   │   │   │   ├── DetalleVenta.java          # Entidad JPA para la relación 'detalle_venta'
│   │   │   │   ├── Producto.java              # Entidad JPA para 'producto' con Named Queries
│   │   │   │   └── Venta.java                 # Entidad JPA para 'venta' y cálculo de totales
│   │   │   ├── repository/                    # Capa de acceso a datos (Spring Data JPA)
│   │   │   │   ├── CategoriaRepository.java   # Repositorio para operaciones sobre categorías
│   │   │   │   ├── ClienteRepository.java     # Métodos JPQL y Named Queries para clientes
│   │   │   │   ├── DetalleVentaRepository.java# Operaciones y conteos de detalles de venta
│   │   │   │   ├── ProductoRepository.java    # Consultas JPQL y Named Queries parametrizadas seguras
│   │   │   │   └── VentaRepository.java       # Consultas por cliente y rango de fechas
│   │   │   └── service/                       # Capa de lógica de negocio y transaccionalidad
│   │   │       ├── CategoriaService.java      # Contrato de servicios para Categorías
│   │   │       ├── ClienteService.java        # Contrato de servicios para Clientes
│   │   │       ├── ProductoService.java       # Contrato de servicios para Productos
│   │   │       ├── VentaService.java          # Contrato de servicios para Ventas
│   │   │       └── impl/                      # Implementaciones de las interfaces de servicio
│   │   │           ├── CategoriaServiceImpl.java # Lógica y validación de borrado de categorías
│   │   │           ├── ClienteServiceImpl.java   # Lógica CRUD, correos únicos y protección de ventas
│   │   │           ├── ProductoServiceImpl.java  # Reglas de sostenibilidad y restricción referencial
│   │   │           └── VentaServiceImpl.java     # Transacciones, validación y descuento de stock
│   │   └── resources/
│   │       ├── application.properties         # Configuración activa de conexión a MySQL 8 y Hibernate
│   │       ├── database/                      # Scripts SQL del sistema
│   │       │   ├── schema.sql                 # DDL de base de datos 'ecocompra_db', tablas y restricciones
│   │       │   └── data.sql                   # DML con datos semilla sostenibles de prueba
│   │       └── static/                        # Archivos estáticos servidos automáticamente
│   │           └── index.html                 # Página de presentación y monitor de estado del sistema
│   └── test/
│       ├── java/com/ecocompra/tech/           # Batería de pruebas automatizadas (JUnit 5)
│       │   ├── EcocompraTechApplicationTests.java # Prueba de arranque del contexto Spring
│       │   ├── controller/
│       │   │   ├── ClienteControllerTest.java # Pruebas de integración HTTP del controlador de clientes
│       │   │   └── ProductoControllerTest.java# Pruebas de integración HTTP del controlador de productos
│       │   └── service/
│       │       ├── ClienteServiceTest.java    # Pruebas CRUD, reglas de negocio y Named Queries de cliente
│       │       ├── ProductoServiceTest.java   # Pruebas CRUD, métricas de sostenibilidad y JPQL de producto
│       │       └── VentaIntegrationTest.java  # Pruebas integradas de ventas, stock y relaciones JPA
│       └── resources/
│           └── application.properties         # Configuración de base de datos H2 en memoria (modo MySQL)
```

---

## 🗄️ Modelo de Datos y Diccionario de Tablas

El modelo entidad-relación conecta 5 entidades principales para el flujo comercial y la categorización sostenible:

```text
[categoria] (1) <--- (N) [producto] (1) <--- (N) [detalle_venta] (N) ---> (1) [venta] (N) ---> (1) [cliente]
```

### Reglas de Integridad Referencial
1. **Protección `ON DELETE RESTRICT`**: No se permite eliminar categorías con productos asociados, ni clientes con ventas registradas, ni productos que figuren en líneas de detalle de ventas.
2. **Validación de Sostenibilidad**:
   - Si se registra `consumo_kwh`, el campo `condiciones_consumo` es obligatorio.
   - Si se informa `consumo_kwh`, `reparabilidad` o `vida_util_anios`, el campo `fuente_tecnica` es obligatorio y verificable.
3. **Consistencia Financiera y de Stock**:
   - `precio` $\ge$ 0, `stock` $\ge$ 0, `cantidad` $>$ 0, `precio_unitario` $\ge$ 0.
   - El precio unitario se almacena en la línea de detalle al momento de la venta, conservando el valor histórico aun si el precio del producto cambia en el catálogo posteriormente.

---

## 🛠️ Configuración de la Base de Datos MySQL

### 1. Ejecución del Script DDL y DML
Se proveen scripts listos para su ejecución en MySQL Server o MySQL Workbench:
- **DDL:** `src/main/resources/database/schema.sql`
- **DML:** `src/main/resources/database/data.sql`

Comando por consola:
```bash
mysql -u root -p < src/main/resources/database/schema.sql
mysql -u root -p < src/main/resources/database/data.sql
```

### 2. Configuración en `application.properties`
```properties
spring.datasource.url=jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:ecocompra_db}?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&createDatabaseIfNotExist=true
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD:root}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect
```

---

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

#### Ejemplo de Petición JSON (`POST /api/productos`):
```json
{
  "idCategoria": 1,
  "nombre": "Laptop VerdeBook Pro 14",
  "descripcion": "Chasis de aluminio reciclado y modular para fácil actualización",
  "precio": 3499.00,
  "stock": 15,
  "reparabilidad": "Calificación iFixit 9/10, módulos RAM y SSD intercambiables",
  "consumoKwh": 0.0450,
  "condicionesConsumo": "8 horas diarias navegación y ofimática a 50% brillo",
  "vidaUtilAnios": 6.00,
  "fuenteTecnica": "https://ecocompra.tech/fichas/verdebook14-spec.pdf"
}
```

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

## 🧪 Pruebas Automatizadas (Actividad 10)

El proyecto cuenta con una batería completa de **31 pruebas automatizadas** que validan el 100% de los requerimientos y garantizan ejecución reproducible en cualquier equipo mediante base de datos en memoria H2 (modo MySQL):

```bash
./mvnw test
```

### Matriz de Casos de Prueba Verificados
- **Registro y validaciones:** Creación de productos y clientes válidos; rechazo de datos negativos, campos nulos o reglas sostenibles ausentes.
- **Búsqueda:** Consultas por clave primaria, casos inexistentes (HTTP 404), consultas JPQL y Named Queries con parámetros seguros enlazados (`:param`).
- **Actualización:** Modificación de datos y persistencia verificada.
- **Eliminación y Restricción Referencial (`ON DELETE RESTRICT`):** Eliminación exitosa de registros sin operaciones vs. rechazo controlado (`IllegalOperationException` / HTTP 409) al intentar borrar productos o clientes asociados a ventas.
- **Ventas y Stock:** Registro transaccional de venta, verificación de claves foráneas, cálculo de total y descuento automático de stock.

---

## 💻 Compilación y Ejecución de la Aplicación

Para iniciar el servidor de desarrollo en el puerto 8080:

```bash
./mvnw spring-boot:run
```

- **Página de inicio:** [http://localhost:8080/](http://localhost:8080/)
- **Endpoint de estado:** [http://localhost:8080/api/estado](http://localhost:8080/api/estado)
