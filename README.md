# EcoCompra Tech — Actividad 05

Integracion de productos, clientes, ventas y usuarios con Angular y Spring Boot. Incluye validaciones, mensajes, administracion de cuentas, permisos y pruebas funcionales con evidencia.

## Iniciar

Abra esta carpeta en VS Code y ejecute desde la raiz en dos terminales:

```powershell
.\scripts\iniciar-campo.ps1
```

```powershell
.\scripts\iniciar-frontend.ps1
```

Abra http://localhost:4200. Usuario admin o vendedor; claves generadas en .local/demo-config.json. Java 21, Node compatible con Angular 22 y pnpm 11.25.0 son requisitos. No ejecute otra version en los mismos puertos.

El perfil campo usa H2 persistente en .local/campo/ecocompra05.mv.db. El perfil demo es temporal. MySQL mantiene la configuracion principal; una base previa necesita aplicar una vez docs/sql/06-usuarios-actividad05.sql. La verificacion de esta entrega se ejecuto con H2 en disco.

## Guia y pruebas

Consulte [docs/actividad05.md](docs/actividad05.md) para el flujo completo, pasos de reproduccion, migracion MySQL y descripcion de los cambios.

```powershell
.\mvnw.cmd verify
cd frontend
pnpm install --frozen-lockfile
pnpm build
```

Desde la raiz, con ambos servidores iniciados:

```powershell
pnpm --dir tests/funcionales install --frozen-lockfile
node tests/funcionales/actividad05.cjs antes
.\scripts\consultar-persistencia.ps1
# Reinicie solo el backend con campo y consulte de nuevo el SQL.
node tests/funcionales/actividad05.cjs despues
node tests/funcionales/seguridad.cjs
node tests/funcionales/errores.cjs
```

Las pruebas usan Microsoft Edge instalado y datos ficticios propios. Sus resultados nuevos quedan por defecto en .local/pruebas05. Las evidencias de la entrega estan en evidencias/actividad05. No se comparte .local, claves ni base de datos.

Usuarios solo esta disponible para ADMIN. No se puede desactivar la propia cuenta administradora. Cambiar permisos revoca sesiones anteriores. El codigo y el informe no se publicaron automaticamente en GitHub.
