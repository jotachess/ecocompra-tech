// Pruebas sobre datos ficticios propios. Requiere backend campo y Angular activos.
const fs = require('fs'), path = require('path'), assert = require('assert/strict'), crypto = require('crypto');
const { chromium } = require(process.env.ECOCOMPRA_PLAYWRIGHT_MODULE || 'playwright');
const root = path.resolve(__dirname, '../..');
const out = process.env.ECOCOMPRA_EVIDENCIAS || path.join(root, '.local/pruebas05');
fs.mkdirSync(out, { recursive: true });
const phase = process.argv[2] || 'antes', cases = [], network = [], pending = [], errors = [];
const credentials = JSON.parse(fs.readFileSync(path.join(root, '.local/demo-config.json'), 'utf8').replace(/^\uFEFF/, ''));
const localPassword = path.join(root, '.local/practica05-password.json');
if (!fs.existsSync(localPassword)) fs.writeFileSync(localPassword, JSON.stringify({ password: crypto.randomBytes(24).toString('base64url') }));
const practicePassword = JSON.parse(fs.readFileSync(localPassword, 'utf8')).password;
const redact = v => Array.isArray(v) ? v.map(redact) : v && typeof v === 'object'
  ? Object.fromEntries(Object.entries(v).map(([k, val]) => [k, /password|token|secret/i.test(k) ? '[omitido]' : redact(val)])) : v;
function record(id, prueba, esperado, obtenido, ok) {
  cases.push({ id, prueba, esperado, obtenido, resultado: ok ? 'Correcto' : 'Falló' });
  assert.ok(ok, `${id}: ${prueba}; obtenido ${obtenido}`);
}
(async () => {
  const browser = await chromium.launch({ channel: 'msedge', headless: true });
  try {
    const ctx = await browser.newContext({ viewport: { width: 1440, height: 1050 }, locale: 'es-PE' });
    const page = await ctx.newPage();page.on('pageerror', e => errors.push(e.message));
    page.on('response', r => {
      if (!r.url().startsWith('http://localhost:8080/api/')) return;
      const req = r.request();pending.push((async () => {
        let body = null;try { body = await r.json(); } catch {}
        network.push({ metodo: req.method(), ruta: new URL(r.url()).pathname, estado: r.status(),
          jwtAdjunto: !!req.headers()['authorization'], solicitud: redact(req.postDataJSON()), respuesta: redact(body) });
      })());
    });
    const shot = name => page.screenshot({ path: path.join(out, name + '.png'), fullPage: true });
    const nav = async name => { await page.getByRole('link', { name: new RegExp(name) }).click();await page.waitForURL('**/' + name.toLocaleLowerCase());await page.waitForLoadState('networkidle'); };
    async function loginUI(name, password) {
      await page.goto('http://localhost:4200/login');await page.locator('#username').fill(name);await page.locator('#password').fill(password);
      await page.getByRole('button', { name: /Entrar al sistema/ }).click();await page.waitForURL('**/inicio');await page.waitForLoadState('networkidle');
    }
    async function token(name, password) {
      const r = await ctx.request.post('http://localhost:8080/api/auth/login', { data: { username: name, password } });
      assert.equal(r.status(), 200);return (await r.json()).accessToken;
    }
    const adminToken = await token('admin', credentials.AdminPassword);
    async function api(resource, id, auth = adminToken) {
      const r = await ctx.request.get(`http://localhost:8080/api/${resource}${id == null ? '' : '/' + id}`, { headers: { Authorization: 'Bearer ' + auth } });
      return { status: r.status(), body: await r.json() };
    }
    async function mutation(id, prueba, method, resource, expected, fn) {
      const awaited = page.waitForResponse(r => new URL(r.url()).pathname.startsWith('/api/' + resource) && r.request().method() === method);
      await fn();const r = await awaited;
      record(id, prueba, `HTTP ${expected}`, `HTTP ${r.status()}`, r.status() === expected);
      if (expected >= 400) await page.getByRole('alert').first().waitFor();else await page.getByRole('status').waitFor();
      await page.waitForLoadState('networkidle');return expected === 204 ? null : r.json();
    }
    const saveClient = () => page.getByRole('button', { name: 'Guardar cliente', exact: true }).click();
    const saveProduct = () => page.getByRole('button', { name: 'Guardar producto', exact: true }).click();
    const saveSale = () => page.getByRole('button', { name: 'Guardar venta', exact: true }).click();
    const saveUser = () => page.getByRole('button', { name: 'Guardar usuario', exact: true }).click();
    const waitDisabled = () => page.locator('button[type="submit"]:disabled').waitFor();
    if (phase === 'antes') {
      await page.goto('http://localhost:4200/usuarios');await page.waitForURL('**/login');
      record('F01', 'Ruta protegida sin sesión', 'Redirección a login', '/login', true);
      await page.locator('#username').fill('admin');await page.locator('#password').fill('incorrecta05');
      const failedLogin = page.waitForResponse(r => r.url().endsWith('/auth/login'));
      await page.getByRole('button', { name: /Entrar al sistema/ }).click();const lr = await failedLogin;
      await page.getByRole('alert').waitFor();record('F02', 'Login incorrecto', '401 y mensaje visible', `HTTP ${lr.status()}`, lr.status() === 401);
      await page.locator('#password').fill('');await shot('01-login-error');
      await loginUI('admin', credentials.AdminPassword);
      record('F03', 'Ingreso ADMIN', 'Inicio y enlace Usuarios', 'Inicio y Usuarios visible', await page.getByRole('link', { name: /Usuarios/ }).isVisible());
      await shot('02-inicio-integrado');await nav('Clientes');
      await page.locator('#cn').fill('   ');await page.locator('#cn').press('Tab');
      await page.getByRole('alert').first().waitFor();
      record('F04', 'Nombre solo espacios', 'Guardar deshabilitado y aviso', 'Formulario bloqueado', await page.getByRole('button', { name: 'Guardar cliente', exact: true }).isDisabled() && await page.getByRole('alert').count() > 0);
      await shot('03-cliente-espacios');
      await page.locator('#cn').fill('Cliente Practica 05');await page.locator('#ce').fill('correo-invalido');await page.locator('#ce').press('Tab');
      record('F05', 'Correo inválido', 'Guardar deshabilitado', 'Formulario bloqueado', await page.getByRole('button', { name: 'Guardar cliente', exact: true }).isDisabled());await shot('04-cliente-correo');
      await page.locator('#ce').fill('practica05@example.com');await page.locator('#ct').fill('900000005');
      const client = await mutation('F06', 'Crear cliente desde Angular', 'POST', 'clientes', 201, saveClient);await shot('05-cliente-creado');
      await page.locator('#cn').fill('Cliente Duplicado 05');await page.locator('#ce').fill('practica05@example.com');
      await mutation('F07', 'Correo duplicado controlado', 'POST', 'clientes', 409, saveClient);
      const duplicated = (await api('clientes')).body.filter(c => c.correo === 'practica05@example.com');
      record('F08', 'Error no conserva éxito ni duplica filas', 'Sin aviso de éxito y un solo cliente', `Clientes coincidentes ${duplicated.length}`, await page.getByRole('status').count() === 0 && duplicated.length === 1);
      await shot('06-cliente-duplicado');await page.getByRole('button', { name: 'Limpiar', exact: true }).click();
      let row = page.getByRole('row').filter({ hasText: 'Cliente Practica 05' });await row.getByRole('button', { name: 'Editar', exact: true }).click();
      await page.getByRole('heading', { name: 'Editar cliente', exact: true }).waitFor();await page.locator('#ct').fill('900000055');
      await mutation('F09', 'Editar cliente', 'PUT', 'clientes', 200, saveClient);
      await nav('Productos');await page.locator('#pn').fill('Teclado Practica 05');await page.locator('#pc').selectOption({ label: 'Equipos de demostración' });
      await page.locator('#pp').fill('-1');await page.locator('#pp').press('Tab');
      record('F10', 'Precio negativo', 'Guardar deshabilitado y aviso', 'Formulario bloqueado', await page.getByRole('button', { name: 'Guardar producto', exact: true }).isDisabled());await shot('07-producto-precio');
      await page.locator('#pp').fill('150');await page.locator('#ps').fill('1.5');await page.locator('#ps').press('Tab');
      record('F11', 'Stock fraccionario', 'Guardar deshabilitado', 'Formulario bloqueado', await page.getByRole('button', { name: 'Guardar producto', exact: true }).isDisabled());await shot('08-producto-stock');
      await page.locator('#ps').fill('10');await page.locator('details summary').click();await page.locator('#pe').fill('1');await page.locator('#pm').fill('Consumo de ejemplo por año');
      await waitDisabled();
      record('F12', 'Sostenibilidad sin fuente', 'Rechazo hasta incluir una fuente', 'Formulario bloqueado', await page.getByRole('button', { name: 'Guardar producto', exact: true }).isDisabled());await shot('09-producto-fuente');
      await page.locator('#pe').fill('');await page.locator('#pm').fill('');await page.locator('details summary').click();
      const product = await mutation('F13', 'Crear producto', 'POST', 'productos', 201, saveProduct);await shot('10-producto-creado');
      await page.getByRole('button', { name: 'Editar Teclado Practica 05', exact: true }).click();await page.getByRole('heading', { name: 'Editar producto', exact: true }).waitFor();await page.locator('#pp').fill('155');
      await mutation('F14', 'Editar precio del producto', 'PUT', 'productos', 200, saveProduct);
      await nav('Ventas');await page.locator('#vc').selectOption({ label: 'Cliente Practica 05' });
      await page.locator('#vp0').selectOption({ label: (await page.locator('#vp0 option').allTextContents()).find(s => s.includes('Teclado Practica 05')) });
      await page.locator('#vq0').fill('0');await page.locator('#vq0').press('Tab');
      record('F15', 'Cantidad cero', 'Guardar deshabilitado', 'Formulario bloqueado', await page.getByRole('button', { name: 'Guardar venta', exact: true }).isDisabled());await shot('11-venta-cantidad');
      await page.locator('#vq0').fill('100');const salesBefore = (await api('ventas')).body.length;
      await mutation('F16', 'Venta con stock insuficiente', 'POST', 'ventas', 409, saveSale);await shot('12-venta-sin-stock');
      const unchanged = (await api('productos', product.idProducto)).body.stock === 10 && (await api('ventas')).body.length === salesBefore;
      record('F17', 'Venta rechazada no altera base', 'Stock 10 y ventas sin cambios', 'Stock y número de ventas conservados', unchanged);
      await page.locator('#vq0').fill('2');const sale = await mutation('F18', 'Registrar venta válida', 'POST', 'ventas', 201, saveSale);await shot('13-venta-creada');
      row = page.getByRole('row').filter({ hasText: 'Cliente Practica 05' });await row.getByRole('button', { name: 'Editar', exact: true }).click();
      await page.getByRole('heading', { name: 'Editar venta', exact: true }).waitFor();await page.locator('#vq0').fill('1');await page.locator('#vq0').press('Tab');
      const updated = await mutation('F19', 'Editar venta y recalcular stock', 'PUT', 'ventas', 200, saveSale);
      record('F20', 'Valores de venta actualizada', 'Una unidad a 155 y stock 9', `Cantidad ${updated.detalles[0].cantidad}`, updated.detalles[0].cantidad === 1 && updated.detalles[0].precioUnitario === 155 && (await api('productos', product.idProducto)).body.stock === 9);
      await row.getByRole('button', { name: 'Ver detalle', exact: true }).click();await shot('14-venta-editada');
      await nav('Clientes');row = page.getByRole('row').filter({ hasText: 'Cliente Practica 05' });await row.getByRole('button', { name: 'Eliminar', exact: true }).click();
      await mutation('F21', 'Eliminar cliente usado en venta', 'DELETE', 'clientes', 409, () => row.getByRole('button', { name: 'Confirmar', exact: true }).click());await shot('15-cliente-restringido');
      await nav('Productos');await page.getByRole('button', { name: 'Eliminar Teclado Practica 05', exact: true }).click();
      await mutation('F22', 'Eliminar producto usado en venta', 'DELETE', 'productos', 409, () => page.getByRole('button', { name: 'Confirmar', exact: true }).click());await shot('16-producto-restringido');
      await nav('Usuarios');await page.locator('#un').fill('x');await page.locator('#up').fill('corta');await page.locator('#up').press('Tab');
      record('F23', 'Usuario y clave inválidos', 'Guardar deshabilitado y mensajes', 'Formulario bloqueado', await page.getByRole('button', { name: 'Guardar usuario', exact: true }).isDisabled() && await page.getByRole('alert').count() >= 2);await shot('17-usuario-validacion');
      await page.locator('#un').fill('practica05');await page.locator('#up').fill(practicePassword);
      const user = await mutation('F24', 'Crear usuario VENDEDOR', 'POST', 'usuarios', 201, saveUser);
      record('F25', 'Respuesta de usuario sin secretos', 'No exponer clave ni hash', 'Solo datos de cuenta', !('password' in user) && !('passwordHash' in user));await shot('18-usuario-creado');
      const firstToken = await token('practica05', practicePassword);
      await page.locator('#un').fill('practica05');await page.locator('#up').fill(practicePassword);
      await mutation('F26', 'Usuario duplicado', 'POST', 'usuarios', 409, saveUser);await shot('19-usuario-duplicado');
      await page.getByRole('button', { name: 'Limpiar', exact: true }).click();
      row = page.getByRole('row').filter({ hasText: 'practica05' });await row.getByRole('button', { name: 'Editar', exact: true }).click();await page.getByRole('heading', { name: 'Editar usuario', exact: true }).waitFor();
      await page.locator('#ur').selectOption('ADMIN');await mutation('F27', 'Cambiar rol de usuario', 'PUT', 'usuarios', 200, saveUser);
      record('F28', 'JWT anterior invalidado por cambio de rol', '401 con JWT previo', `HTTP ${(await api('productos', null, firstToken)).status}`, (await api('productos', null, firstToken)).status === 401);
      const newAdmin = await token('practica05', practicePassword);
      record('F29', 'Nuevo login aplica el rol actualizado', '200 al listar usuarios', `HTTP ${(await api('usuarios', null, newAdmin)).status}`, (await api('usuarios', null, newAdmin)).status === 200);
      await row.getByRole('button', { name: 'Editar', exact: true }).click();await page.getByRole('heading', { name: 'Editar usuario', exact: true }).waitFor();await page.locator('#ur').selectOption('VENDEDOR');
      await mutation('F30', 'Restaurar rol VENDEDOR', 'PUT', 'usuarios', 200, saveUser);
      const beforeDisable = await token('practica05', practicePassword);
      await row.getByRole('button', { name: 'Editar', exact: true }).click();await page.getByRole('heading', { name: 'Editar usuario', exact: true }).waitFor();await page.locator('#ua').selectOption({ label: 'Inactivo' });
      await mutation('F31', 'Desactivar usuario', 'PUT', 'usuarios', 200, saveUser);await shot('20-usuario-inactivo');
      const inactiveLogin = await ctx.request.post('http://localhost:8080/api/auth/login', { data: { username: 'practica05', password: practicePassword } });
      record('F32', 'Cuenta inactiva no puede ingresar', '401 en login y token anterior', `Login HTTP ${inactiveLogin.status()}`, inactiveLogin.status() === 401 && (await api('productos', null, beforeDisable)).status === 401);
      await row.getByRole('button', { name: 'Editar', exact: true }).click();await page.getByRole('heading', { name: 'Editar usuario', exact: true }).waitFor();await page.locator('#ua').selectOption({ label: 'Activo' });
      await mutation('F33', 'Reactivar cuenta', 'PUT', 'usuarios', 200, saveUser);
      record('F34', 'Reactivar no revive el JWT anterior', '401 con token previo', `HTTP ${(await api('productos', null, beforeDisable)).status}`, (await api('productos', null, beforeDisable)).status === 401);
      const self = page.getByRole('row').filter({ has: page.locator('strong', { hasText: /^admin$/ }) });await self.getByRole('button', { name: 'Editar', exact: true }).click();
      await page.getByRole('heading', { name: 'Editar usuario', exact: true }).waitFor();await page.locator('#ua').selectOption({ label: 'Inactivo' });
      await mutation('F35', 'Impedir autodesactivación del administrador', 'PUT', 'usuarios', 409, saveUser);await shot('21-admin-protegido');
      await page.getByRole('button', { name: 'Cancelar', exact: true }).click();
      const sellerContext = await browser.newContext({ viewport: { width: 1440, height: 1050 } });const sellerPage = await sellerContext.newPage();
      await sellerPage.goto('http://localhost:4200/login');await sellerPage.locator('#username').fill('vendedor');await sellerPage.locator('#password').fill(credentials.VendedorPassword);
      await sellerPage.getByRole('button', { name: /Entrar al sistema/ }).click();await sellerPage.waitForURL('**/inicio');await sellerPage.waitForLoadState('networkidle');
      record('F36', 'Vendedor no ve Usuarios', 'Sin enlace administrativo', 'Usuarios oculto', await sellerPage.getByRole('link', { name: /Usuarios/ }).count() === 0);
      // Navegación del router en una sesión en memoria, sin recargar ni borrar la sesión.
      await sellerPage.evaluate(() => { history.pushState({}, '', '/usuarios');dispatchEvent(new PopStateEvent('popstate')); });
      await sellerPage.waitForURL('**/inicio');record('F37', 'Guard de Usuarios rechaza vendedor', 'Redirección a Inicio', '/inicio', true);
      await sellerPage.screenshot({ path: path.join(out, '22-vendedor-acceso.png'), fullPage: true });await sellerContext.close();
      await nav('Clientes');await page.route('http://localhost:8080/api/clientes', route => route.abort('connectionfailed'));
      await nav('Inicio');await nav('Clientes');await page.getByRole('alert').filter({ hasText: 'No se pudo conectar' }).waitFor();
      record('F38', 'Desconexión simulada controlada', 'Mensaje de conexión sin caída de la pantalla', 'Mensaje visible', true);await shot('23-error-conexion');
      await page.unroute('http://localhost:8080/api/clientes');await nav('Inicio');await nav('Clientes');
      await page.getByRole('row').filter({ hasText: 'Cliente Practica 05' }).waitFor();
      record('F39', 'Recuperación de conexión', 'Listado vuelve a mostrar cliente', 'Cliente visible', await page.getByRole('row').filter({ hasText: 'Cliente Practica 05' }).count() === 1);
      fs.writeFileSync(path.join(out, 'ids-prueba.json'), JSON.stringify({ cliente: client.idCliente, producto: product.idProducto, venta: sale.idVenta, usuario: user.id }, null, 2));
    } else {
      const ids = JSON.parse(fs.readFileSync(path.join(out, 'ids-prueba.json'), 'utf8'));
      for (const [id, resource, key] of [[ids.cliente, 'clientes', 'idCliente'], [ids.producto, 'productos', 'idProducto'], [ids.venta, 'ventas', 'idVenta'], [ids.usuario, 'usuarios', 'id']]) {
        const r = await api(resource, id);let ok = r.status === 200 && r.body[key] === id;
        if (resource === 'clientes') ok = ok && r.body.telefono === '900000055';
        if (resource === 'productos') ok = ok && r.body.precio === 155 && r.body.stock === 9;
        if (resource === 'ventas') ok = ok && r.body.detalles[0].cantidad === 1 && r.body.detalles[0].precioUnitario === 155;
        if (resource === 'usuarios') ok = ok && r.body.rol === 'VENDEDOR' && r.body.activo;
        record('P-' + resource, 'Persistencia de ' + resource + ' tras reinicio', 'Mismo ID y valores guardados', `HTTP ${r.status}, ID ${id}`, ok);
      }
      await loginUI('admin', credentials.AdminPassword);await nav('Usuarios');await shot('24-usuarios-persistentes');await nav('Ventas');
      let row = page.getByRole('row').filter({ hasText: 'Cliente Practica 05' });await row.getByRole('button', { name: 'Ver detalle', exact: true }).click();await shot('25-venta-persistente');
      await row.getByRole('button', { name: 'Eliminar', exact: true }).click();await mutation('D01', 'Eliminar venta de prueba', 'DELETE', 'ventas', 204, () => row.getByRole('button', { name: 'Confirmar', exact: true }).click());
      record('D02', 'Eliminar venta restituye stock', 'Stock vuelve a 10', `Stock ${(await api('productos', ids.producto)).body.stock}`, (await api('productos', ids.producto)).body.stock === 10);
      await nav('Productos');await page.getByRole('button', { name: 'Eliminar Teclado Practica 05', exact: true }).click();
      await mutation('D03', 'Eliminar producto libre de ventas', 'DELETE', 'productos', 204, () => page.getByRole('button', { name: 'Confirmar', exact: true }).click());
      await nav('Clientes');row = page.getByRole('row').filter({ hasText: 'Cliente Practica 05' });await row.getByRole('button', { name: 'Eliminar', exact: true }).click();
      await mutation('D04', 'Eliminar cliente libre de ventas', 'DELETE', 'clientes', 204, () => row.getByRole('button', { name: 'Confirmar', exact: true }).click());
      const userToken = await token('practica05', practicePassword);await nav('Usuarios');row = page.getByRole('row').filter({ hasText: 'practica05' });await row.getByRole('button', { name: 'Eliminar', exact: true }).click();
      await mutation('D05', 'Eliminar usuario de prueba', 'DELETE', 'usuarios', 204, () => row.getByRole('button', { name: 'Confirmar', exact: true }).click());await shot('26-limpieza-exitosa');
      record('D06', 'Usuario eliminado pierde acceso', '401 con JWT anterior', `HTTP ${(await api('productos', null, userToken)).status}`, (await api('productos', null, userToken)).status === 401);
      for (const [resource, id] of [['clientes', ids.cliente], ['productos', ids.producto], ['ventas', ids.venta], ['usuarios', ids.usuario]]) {
        const r = await api(resource, id);record('D-' + resource, 'Consultar ' + resource + ' después de DELETE', 'HTTP 404', `HTTP ${r.status}`, r.status === 404);
      }
    }
    record('JS-' + phase, 'Sin errores JavaScript en el recorrido ' + phase, 'Cero excepciones de página', `${errors.length} errores`, errors.length === 0);
  } finally {
    await Promise.allSettled(pending);fs.writeFileSync(path.join(out, `casos-${phase}.json`), JSON.stringify(cases, null, 2));
    fs.writeFileSync(path.join(out, `http-${phase}.json`), JSON.stringify(network, null, 2));await browser.close();
  }
  console.log(JSON.stringify({ fase: phase, casos: cases.length, solicitudesAngular: network.length, errores: errors.length }));
})().catch(e => { console.error(e);process.exit(1); });
