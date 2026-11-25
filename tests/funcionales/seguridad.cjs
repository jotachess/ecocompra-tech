const fs=require('fs'),path=require('path'),assert=require('assert/strict');
const root=path.resolve(__dirname,'../..');
const out=process.env.ECOCOMPRA_EVIDENCIAS||path.join(root,'.local/pruebas05');fs.mkdirSync(out,{recursive:true});
const conf=JSON.parse(fs.readFileSync(path.join(root,'.local/demo-config.json'),'utf8').replace(/^\uFEFF/,''));
const rows=[];
async function request(id,prueba,path,method,expected,token,body,headers={}) {
 const r=await fetch('http://localhost:8080'+path,{method,headers:{...(token?{Authorization:'Bearer '+token}:{}),...(body?{'Content-Type':'application/json'}:{}),...headers},...(body?{body:JSON.stringify(body)}:{})});
 rows.push({id,prueba,metodo:method,ruta:path,esperado:expected,obtenido:r.status,resultado:r.status===expected?'Correcto':'Falló'});assert.equal(r.status,expected,prueba);return r;
}
async function login(username,password) {
 const r=await fetch('http://localhost:8080/api/auth/login',{method:'POST',headers:{'Content-Type':'application/json'},body:JSON.stringify({username,password})});assert.equal(r.status,200);return (await r.json()).accessToken;
}
(async()=>{
 const admin=await login('admin',conf.AdminPassword),seller=await login('vendedor',conf.VendedorPassword);
 for(const resource of ['productos','clientes','ventas','usuarios']) {
  await request('S-anon-'+resource,'Sin token en '+resource,'/api/'+resource,'GET',401);
  await request('S-admin-'+resource,'ADMIN consulta '+resource,'/api/'+resource,'GET',200,admin);
  await request('S-seller-'+resource,'VENDEDOR consulta '+resource,'/api/'+resource,'GET',resource==='usuarios'?403:200,seller);
 }
 for(const [method,url] of [['POST','/api/usuarios'],['PUT','/api/usuarios/1'],['DELETE','/api/usuarios/1'],['GET','/api/usuarios/1'],['POST','/api/productos']])
  await request('S-rol-'+method+url,'Operacion restringida para VENDEDOR',url,method,403,seller,['POST','PUT'].includes(method)?{}:undefined);
 await request('S-validacion','Validacion backend aunque se omita Angular','/api/clientes','POST',400,admin,{nombre:' ',correo:'correo-invalido'});
 await request('S-inexistente','Recurso inexistente','/api/productos/99999999','GET',404,admin);
 const cors=await request('S-cors-ok','Preflight autorizada','/api/clientes','OPTIONS',200,null,null,{Origin:'http://localhost:4200','Access-Control-Request-Method':'POST','Access-Control-Request-Headers':'authorization,content-type'});
 assert.equal(cors.headers.get('access-control-allow-origin'),'http://localhost:4200');
 await request('S-cors-no','Origen no autorizado','/api/clientes','OPTIONS',403,null,null,{Origin:'http://no-autorizado.invalid','Access-Control-Request-Method':'GET'});
 console.log(JSON.stringify({comprobaciones:rows.length,fallos:0}));
})().catch(e=>{console.error(e);process.exitCode=1}).finally(()=>fs.writeFileSync(path.join(out,'seguridad.json'),JSON.stringify(rows,null,2)));
