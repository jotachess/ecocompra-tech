# Actividad 04 — integración Angular y Spring Boot

La entrega parte de la Actividad 03. Conserva JWT, roles, validaciones y CRUD del backend. Agrega servicios Angular por módulo y un perfil de práctica con H2 persistente en disco.

## Iniciar esta versión

Abra en VS Code la carpeta `actividad04/ecocompra-tech` que contiene este documento y `pom.xml`. No inicie simultáneamente otras versiones del proyecto en los puertos 8080 y 4200.

En una terminal PowerShell, desde la raíz del proyecto:

```powershell
.\scripts\iniciar-campo.ps1
```

En otra terminal, desde la misma raíz:

```powershell
.\scripts\iniciar-frontend.ps1
```

Abra http://localhost:4200. Usuario `admin` o `vendedor`; consulte la clave correspondiente en `.local/demo-config.json`. El primer arranque genera ese archivo y las cuentas. No comparta las claves. Los scripts detectan herramientas locales disponibles; en otro equipo se requieren Java 21, Node compatible con Angular 22 y pnpm 11.25.0. `entorno-local.ps1` solo ajusta el PATH del proceso; no instala programas ni cambia Windows.

## Archivos principales

- `frontend/src/app/core/services/productos.service.ts`: GET de lista e identificador, POST, PUT y DELETE de productos y GET de categorías.
- `frontend/src/app/core/services/clientes.service.ts`: operaciones de clientes.
- `frontend/src/app/core/services/ventas.service.ts`: operaciones de ventas.
- `frontend/src/app/core/models.ts`: contratos tipados de respuestas y solicitudes y URL base de la API.
- `frontend/src/app/core/http-error.ts`: interpreta mensajes HTTP y validaciones para los componentes.
- `frontend/src/app/app.config.ts`: registra HttpClient e interceptor.
- `frontend/src/app/core/auth.interceptor.ts`: adjunta JWT exclusivamente a la API propia y redirige al login ante 401.
- `frontend/src/app/app.routes.ts`: carga de módulos y protección mediante authGuard.
- `src/main/resources/application-campo.properties`: H2 en disco; `update` es exclusivo de la práctica local.
- `docs/sql/05-verificacion-persistencia.sql`: consultas de lectura para comprobar los registros de prueba.

Los componentes Inicio, Productos, Clientes y Ventas consumen los servicios por inyección de dependencias. Las operaciones retornan Observable y se ejecutan al suscribirse. Los formularios envían JSON y actualizan el listado tras la respuesta satisfactoria. En ventas, la transacción del servidor conserva los precios del detalle y ajusta el stock.

## Perfiles de base de datos

`campo` guarda la base en `.local/campo/ecocompra.mv.db`. Los datos permanecen después de reiniciar. DemoData crea datos ficticios únicamente cuando no hay categorías. El perfil `demo` sigue usando memoria y pierde los registros al detenerse. El perfil predeterminado sigue configurado para MySQL con `DB_USER`, `DB_PASSWORD`, `JWT_SECRET` y las claves iniciales en el entorno. Las verificaciones nuevas de esta actividad se ejecutaron en H2 en disco, no en MySQL.

## Repetir la comprobación de persistencia manualmente

1. Ingrese como admin y cree un cliente con correo de ejemplo único.
2. Cree un producto de práctica con precio 150 y stock 10; edite el precio a 155.
3. Cree una venta de dos unidades; edítela a una unidad. El total queda en S/ 155 y el stock en 9.
4. Anote los identificadores y consulte las tablas con las consultas SQL de la entrega, adaptando el correo y el nombre.
5. Detenga solo el backend con Ctrl+C y vuelva a ejecutar `iniciar-campo.ps1`. No elimine `.local` ni cambie al perfil demo.
6. Actualice el navegador, ingrese nuevamente si es necesario y compruebe los mismos registros, valores e identificadores.
7. Consulte de nuevo las tablas. Para retirar la prueba, elimine primero la venta, después el producto y finalmente el cliente. La venta eliminada devuelve el stock a 10.

La ejecución documentada usó cliente 4, producto 5 y venta 3. Se retiraron al finalizar. Las cifras de identificadores pueden cambiar al repetir la prueba.

Para ejecutar las consultas SQL desde PowerShell, con los datos de práctica todavía registrados, use `./scripts/consultar-persistencia.ps1`. Puede ejecutarse antes y después del reinicio. El script lee el SQL entregado y se conecta al archivo H2 existente; si adapta los nombres de prueba, adapte también los filtros SQL. Si ya retiró los registros, las consultas devuelven cero filas.

## Verificación técnica y evidencia

```powershell
.\mvnw.cmd verify
cd frontend
pnpm install --frozen-lockfile
pnpm build
```

La ejecución del 15 de noviembre de 2026 registró 76 pruebas backend sin fallos, compilación Angular satisfactoria, 23 comprobaciones de navegador y persistencia y cinco comprobaciones adicionales de seguridad/CORS. Se capturaron 47 solicitudes reales de Angular. Los registros están en `evidencias/actividad04`.

Las capturas 01 a 11 son pantallas reales de la aplicación. Las capturas técnicas posteriores muestran archivos fuente o registros reales en un visor de evidencia; no son capturas del IDE ni de DevTools. Los JSON HTTP omiten credenciales y tokens. No se documentan aquí pruebas unitarias Angular ni un nuevo ensayo con MySQL.

## Entrega y GitHub

Comparta el informe y el ZIP de fuentes. El ZIP excluye `.local`, credenciales, base local, `.git`, `node_modules`, `target`, `.angular` y `dist`. Cada compañero generará su base de práctica y sus claves al iniciar. Para publicar en el repositorio compartido, incorpore estos cambios en un checkout de Git y use una rama de Actividad 04; la entrega local no se publica automáticamente.
