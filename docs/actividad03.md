# Actividad 03 Seguridad y Angular

Base: `jotachess/ecocompra-tech`, commit
`460339671781df323dc96b1b09ce45475e5effec`. Trabajo preparado en la rama local
`feature/actividad-03`. La entrega no implica una publicación en GitHub.

## Ejecutar la demostración

Requisitos: Java 21 (`JAVA_HOME`), Node 24.22.1 o una versión compatible con
Angular 22 y pnpm 11.25.0. Maven Wrapper está incluido. La primera ejecución
descarga dependencias y requiere Internet.

Desde la raíz del proyecto, en PowerShell:

```powershell
.\scripts\iniciar-demo.ps1
```

En otra terminal, desde la misma raíz:

```powershell
.\scripts\iniciar-frontend.ps1
```

Abrir http://localhost:4200. Usuarios `admin` y `vendedor`; sus claves aleatorias
están en `.local/demo-config.json`, creado por el primer script. Consultarlo
localmente para iniciar sesión. No se incluyen claves reales ni JWT en la entrega.
Si Windows bloquea un script descargado, revisar su contenido y ejecutar desde
una terminal con una política de ejecución autorizada para esa sesión.

La API escucha en http://localhost:8080. El perfil `demo` utiliza H2 en memoria,
crea tres productos, dos clientes y una venta ficticios. No conecta a MySQL.
Los datos de H2 desaparecen al detener el backend; las claves locales se reutilizan.
Detener cada proceso con Ctrl+C en su terminal. La página `/` del backend es la
página de bienvenida heredada; la interfaz Angular funciona en el puerto 4200.

## Usar la base MySQL existente

Mantener las instrucciones de persistencia de la Actividad 02. Para una base ya
creada, aplicar `docs/sql/04-seguridad-actividad03.sql`; no recrear sus tablas de
negocio. Para una base nueva, `01-esquema.sql` ya incluye la tabla `usuario`.
El perfil predeterminado usa MySQL con `ddl-auto=validate`.

Configurar `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` (al menos 32 bytes),
`ADMIN_PASSWORD` y `VENDEDOR_PASSWORD` (12 a 72 bytes UTF-8) en el entorno antes
de ejecutar `mvnw.cmd spring-boot:run`. Las claves de las cuentas iniciales se
cifran mediante hash BCrypt. El arranque sólo crea las cuentas que aún no existen;
cambiar las variables no modifica una cuenta existente. No hay registro público
ni pantalla de administración de usuarios en esta entrega.

`CORS_ORIGINS` admite orígenes exactos separados por coma; por defecto
`http://localhost:4200`. La URL cliente está en
`frontend/src/app/core/models.ts` (`API_URL`). Cambiar ambas configuraciones si cambia
el origen de la aplicación. H2 permite verificar la práctica sin intervenir la
base previa; esta sesión no ejecutó una validación adicional en MySQL.

## Permisos y contrato

| Operación | ADMIN | VENDEDOR |
| --- | --- | --- |
| Consultar categorías, productos, clientes y ventas | Sí | Sí |
| Crear, editar o eliminar categorías/productos | Sí | No |
| Crear y editar clientes | Sí | Sí |
| Eliminar clientes | Sí | No |
| Registrar ventas | Sí | Sí |
| Modificar y eliminar ventas | Sí | No |

`POST /api/auth/login` recibe `username` y `password`; devuelve un JWT HS256,
`tokenType`, `expiresIn`, `username` y `roles`. Duración: 1800 segundos. Se verifican
firma, vigencia, emisor `ecocompra-tech` y audiencia `ecocompra-api`.
Enviar `Authorization: Bearer <token>` en cada solicitud de negocio.
`GET /api/auth/me` permite consultar la identidad autenticada. `/api/estado` es público.

La API es stateless y no usa cookies de autenticación. Angular conserva el token
sólo en memoria y lo agrega únicamente a las peticiones de esta API. Al recargar
la página se debe iniciar sesión de nuevo. Cerrar sesión elimina el token del
cliente; un token previamente emitido sigue vigente hasta su expiración. No hay
refresh token ni revocación del lado servidor en este alcance.

Los cuatro recursos tienen `GET /api/{recurso}`, `GET /api/{recurso}/{id}`,
`POST`, `PUT /{id}` y `DELETE /{id}`. Consultas/actualización: 200; creación:
201 con Location; eliminación: 204; validación: 400; sin autenticación: 401;
permiso insuficiente: 403; inexistente: 404; duplicados/stock/relaciones: 409.
Las excepciones se presentan como JSON; no se exponen trazas ni errores SQL.

La modificación de una venta restituye sus cantidades anteriores y aplica el
nuevo detalle en una transacción. Eliminar una venta restituye el stock y borra
sus detalles. Se usan bloqueos pesimistas en venta/productos para proteger las
existencias. Esta operación física, restringida a ADMIN, satisface el CRUD de la
práctica; no implementa una anulación contable con historial de auditoría.

Los atributos de sostenibilidad son opcionales; si se declaran métricas se exige
su fuente técnica y, para consumo, las condiciones de medición. Los productos de
demostración no inventan métricas ambientales. La interfaz los muestra pendientes
de documentación.

## Estructura Angular

Angular 22.2.4, CLI 22.2.5 y TypeScript 6.0.3. Componentes standalone, rutas
protegidas con carga diferida y formularios reactivos. `core/` contiene modelos,
servicios HTTP, sesión, interceptor y guard. `features/` contiene login, inicio,
productos, clientes y ventas. `styles.scss` utiliza SCSS, CSS Grid y Flexbox.
Se usan interpolación, property binding, eventos, `NgClass`, `formGroup`,
`formControlName`, `@if` y `@for`. Estos dos últimos son bloques de control de
flujo de Angular moderno, no las antiguas directivas `*ngIf` y `*ngFor`.

## Verificación de esta entrega

```powershell
.\mvnw.cmd test
cd frontend
pnpm install --frozen-lockfile
pnpm build
```

Resultados locales: **76 pruebas del backend, cero fallos y cero errores**, con
perfil de prueba H2; **29 solicitudes HTTP reales** con estados esperados;
compilación de producción Angular con plantillas estrictas, sin advertencias.
Se verificaron en navegador login, navegación, correo inválido, creación de
cliente, rechazo por stock, creación de venta y vista de vendedor. No se afirma
haber ejecutado pruebas unitarias de Angular ni pruebas de navegador móvil.

Los registros HTTP saneados se encuentran en `docs/actividad03/`. La carpeta
`Evidencias` del paquete académico incluye capturas reales y el JSON completo
de las respuestas, con contraseñas y tokens omitidos.

Referencias técnicas: [JWT de Spring Security](https://docs.spring.io/spring-security/reference/servlet/oauth2/resource-server/jwt.html),
[compatibilidad Angular](https://angular.dev/reference/versions),
[binding Angular](https://angular.dev/guide/templates/binding).
