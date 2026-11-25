# Actividad 05 — integración completa y pruebas funcionales

La entrega integra productos, clientes, ventas y usuarios. Se conservó la Actividad 04 y se trabajó en `actividad05/ecocompra-tech`. Los nuevos endpoints de usuarios solo admiten ADMIN; los datos de respuesta nunca incluyen contraseña, hash ni versión interna del token.

## Arranque

Abra esta carpeta, la que contiene `pom.xml`, en Visual Studio Code. En una terminal PowerShell:

```powershell
.\scripts\iniciar-campo.ps1
```

En otra terminal desde la misma raíz:

```powershell
.\scripts\iniciar-frontend.ps1
```

Abra http://localhost:4200. Las cuentas `admin` y `vendedor` tienen sus claves locales en `.local/demo-config.json`. Se generan al primer arranque. Los scripts detectan herramientas disponibles en este equipo; en otro equipo se requieren Java 21, Node compatible con Angular 22 y pnpm 11.25.0. No ejecute otras versiones en los puertos 8080 y 4200. Detenga cada servidor con Ctrl+C en su terminal.

## Base y perfiles

El perfil `campo` guarda H2 en `.local/campo/ecocompra05.mv.db`. Se verificó persistencia con ese archivo, no con MySQL. El perfil `demo` sigue siendo temporal en memoria. El perfil predeterminado mantiene MySQL con variables DB_USER, DB_PASSWORD, JWT_SECRET, ADMIN_PASSWORD y VENDEDOR_PASSWORD y ddl-auto=validate.

Para una base MySQL nueva, use `docs/sql/01-esquema.sql`, que ya incluye `usuario.token_version`. Para una base de A03/A04 existente, aplique **una sola vez** `docs/sql/06-usuarios-actividad05.sql` antes de iniciar esta versión. Esa migración no se ejecutó contra MySQL durante la validación de esta entrega. Los tokens previos al cambio de versión requieren un nuevo login.

## Funciones y cambios

- Administración de usuarios: GET y POST `/api/usuarios`; GET, PUT y DELETE `/api/usuarios/{id}`. Alta con username, contraseña y rol. Edición de rol y estado; username queda fijo. PasswordEncoder guarda BCrypt.
- Protección de cuenta: no se permite desactivar, degradar o eliminar la propia cuenta administradora. Se conserva al menos un ADMIN activo. El servidor verifica uid, rol y versión de permisos en cada JWT; cambiar rol o estado revoca tokens anteriores. Eliminar y volver a crear el mismo username no permite reutilizar el token viejo.
- Navegación: Usuarios solo aparece para ADMIN y tiene adminGuard. La API comprueba los permisos aunque se omita Angular.
- Validaciones: nombre no vacío ni solo espacios; formato de correo; precio y stock; precisión de datos técnicos; cantidades positivas de venta; username y contraseña. FieldError muestra los errores junto al control y usa detección Eager para actualizar el estado del mismo FormControl en Angular 22.
- Mensajes: se limpian avisos anteriores al iniciar una mutación, se evita repetir el envío mientras está pendiente y finalize libera el bloqueo. apiError interpreta validaciones, mensajes de backend, desconexión y textos de respaldo para errores sin cuerpo útil.

## Flujo completo

El formulario reactivo valida y compone un DTO. El servicio Angular usa HttpClient; el interceptor adjunta JWT a la API propia. Spring Security comprueba firma, vigencia y cuenta actual. El controlador valida el cuerpo con Bean Validation; el servicio aplica reglas de negocio dentro de la transacción, el repositorio JPA accede a H2/MySQL y el controlador devuelve el estado HTTP. Angular actualiza el listado y el mensaje únicamente al recibir la respuesta. Los rechazos 400/401/403/404/409 no se presentan como éxitos.

La venta valida el stock y sus detalles de forma transaccional. Un alta rechazada por stock insuficiente no crea la venta ni descuenta inventario. La edición devuelve el stock anterior antes de aplicar el nuevo detalle y la eliminación repone las unidades.

## Verificación automática backend y build

```powershell
.\mvnw.cmd verify
cd frontend
pnpm install --frozen-lockfile
pnpm build
```

Se registraron 93 pruebas backend (76 existentes y 17 nuevas de usuarios) sin fallos ni errores y build Angular sin advertencias. Los nuevos tests están en `src/test/java/com/ecocompra/tech/UsuariosIntegrationTest.java`. Se conservan los tests de seguridad, persistencia, servicios y controladores.

## Repetir el recorrido funcional

Use una base de práctica local. El recorrido crea datos ficticios con nombres `practica05`, `Cliente Practica 05` y `Teclado Practica 05` y correo `practica05@example.com`. No debe existir ya esa prueba. El script crea su contraseña aleatoria en `.local/practica05-password.json`; no la comparta. La fase de limpieza solo elimina los identificadores creados por el mismo recorrido.

Con backend `campo` y Angular activos, abra una tercera terminal en la raíz:

```powershell
pnpm --dir tests/funcionales install --frozen-lockfile
node tests/funcionales/actividad05.cjs antes
.\scripts\consultar-persistencia.ps1
```

La automatización utiliza Microsoft Edge instalado. Los resultados se guardan por defecto en `.local/pruebas05`. No es necesario instalar Angular globalmente. La dependencia Playwright está separada de la aplicación y fijada por su lockfile.

Detenga **solo el backend**, vuelva a iniciarlo con `iniciar-campo.ps1` y consulte de nuevo el SQL antes de limpiar. Luego, desde la raíz:

```powershell
node tests/funcionales/actividad05.cjs despues
node tests/funcionales/seguridad.cjs
node tests/funcionales/errores.cjs
```

La fase `despues` comprueba los mismos IDs y valores y retira primero la venta, luego el producto y cliente y finalmente el usuario. `seguridad.cjs` realiza 21 solicitudes de control. `errores.cjs` simula una respuesta 409 con validaciones vacías y una respuesta 503 sin mensaje, y comprueba la recuperación real. La desconexión de F38 también se simula en el navegador. Estas simulaciones no representan fallas provocadas del backend ni cambios de base.

El recorrido documentado pasó 55 comprobaciones de integración/persistencia (40 antes y 15 después), 21 controles HTTP y tres casos adicionales de manejo de errores. Capturó 76 respuestas a solicitudes de Angular durante los dos recorridos. Las lecturas HTTP de control, SQL y simulaciones se contabilizan por separado. No se ejecutaron pruebas unitarias Angular ni una prueba concurrente de carga.

## Evidencias

`evidencias/actividad05` incluye capturas de aplicación, JSON con cada prueba y sus resultados, peticiones HTTP sanitizadas, salidas SQL antes/después/limpieza, resumen de reinicio, logs Maven y build. Las vistas técnicas posteriores se generan desde esos archivos reales y se identifican como vistas de registro o código, no como capturas del IDE o DevTools.

Los registros de prueba se retiraron después de la verificación. Durante la ejecución final fueron usuario 4, cliente 5, producto 5 y venta 3. Los IDs pueden cambiar al repetir. Las claves y tokens no aparecen en el informe ni en los registros compartidos.

## Entrega y publicación

El paquete excluye `.local`, claves, base de datos, `.git`, node_modules de frontend y pruebas, target, dist y .angular. Contiene fuentes, tests, lockfiles, scripts, SQL y evidencia. Cada compañero genera su entorno local al arrancar. La entrega no se publicó automáticamente en GitHub; para hacerlo incorpore los cambios en el checkout del repositorio compartido y use una rama de Actividad 05.
