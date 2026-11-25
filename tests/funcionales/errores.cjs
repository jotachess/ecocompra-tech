// Simula respuestas de error y desconexion en el navegador. No modifica la base.
const fs=require('fs'),path=require('path'),assert=require('assert/strict');
const {chromium}=require('playwright');
const root=path.resolve(__dirname,'../..'),out=process.env.ECOCOMPRA_EVIDENCIAS||path.join(root,'.local/pruebas05');
fs.mkdirSync(out,{recursive:true});
const credentials=JSON.parse(fs.readFileSync(path.join(root,'.local/demo-config.json'),'utf8').replace(/^\uFEFF/,''));
const cases=[];
(async()=>{
 const browser=await chromium.launch({channel:'msedge',headless:true});
 try {
  const page=await browser.newPage({viewport:{width:1440,height:1050}});
  await page.goto('http://localhost:4200/login');await page.locator('#username').fill('admin');await page.locator('#password').fill(credentials.AdminPassword);
  await page.getByRole('button',{name:/Entrar al sistema/}).click();await page.waitForURL('**/inicio');await page.waitForLoadState('networkidle');
  async function simulate(id,status,payload,expected,file){
    await page.route('http://localhost:8080/api/clientes',route=>route.fulfill({status,contentType:'application/json',body:JSON.stringify(payload)}));
    await page.getByRole('link',{name:/Clientes/}).click();await page.waitForURL('**/clientes');
    const message=page.getByRole('alert').filter({hasText:expected});await message.waitFor();
    cases.push({id,prueba:status===409?'Validaciones vacias usan mensaje del backend':'Error 503 sin mensaje usa texto de respaldo',tipo:'Simulacion de respuesta HTTP en el navegador',esperado:expected,obtenido:await message.innerText(),resultado:'Correcto'});
    await page.screenshot({path:path.join(out,file+'.png'),fullPage:true});await page.unroute('http://localhost:8080/api/clientes');
    await page.getByRole('link',{name:/Inicio/}).click();await page.waitForURL('**/inicio');await page.waitForLoadState('networkidle');
  }
  await simulate('E01',409,{validaciones:{},mensaje:'Conflicto simulado de prueba. Intenta nuevamente.'},'Conflicto simulado de prueba. Intenta nuevamente.','27-error-respuesta-vacia');
  await simulate('E02',503,{},'El servidor no pudo completar la operación. Intenta nuevamente.','28-error-servidor-simulado');
  await page.getByRole('link',{name:/Clientes/}).click();await page.waitForURL('**/clientes');await page.waitForLoadState('networkidle');
  assert.equal(await page.getByRole('alert').count(),0);
  cases.push({id:'E03',prueba:'Recuperar servicio despues de retirar la simulacion',tipo:'Peticion real',esperado:'Listado sin alerta',obtenido:'Listado sin alerta',resultado:'Correcto'});
  console.log(JSON.stringify({casos:cases.length,fallos:0}));
 }finally {await browser.close();fs.writeFileSync(path.join(out,'casos-errores-simulados.json'),JSON.stringify(cases,null,2));}
})().catch(e=>{console.error(e);process.exit(1)});
