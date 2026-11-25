package com.ecocompra.tech;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.test.web.servlet.MockMvc;
import com.ecocompra.tech.security.*;
import com.ecocompra.tech.repository.*;
import tools.jackson.databind.ObjectMapper;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest @AutoConfigureMockMvc
class SecurityApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired ObjectMapper mapper;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired JwtEncoder jwtEncoder;
    @Autowired DetalleVentaRepository detalles;
    @Autowired VentaRepository ventas;
    @Autowired ProductoRepository productos;
    @Autowired CategoriaRepository categorias;
    @Autowired ClienteRepository clientes;
    private static final String PASSWORD="ClavePruebasSegura27";

    @BeforeEach void preparar() {
        detalles.deleteAll(); ventas.deleteAll(); productos.deleteAll(); categorias.deleteAll(); clientes.deleteAll();
        usuarios.deleteAll();
        for(Rol rol:Rol.values()) {
            Usuario u=new Usuario();u.setUsername(rol.name().toLowerCase());u.setRol(rol);
            u.setPasswordHash(encoder.encode(PASSWORD));usuarios.save(u);
        }
    }

    String token(String username) throws Exception {
        String body=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\""+username+"\",\"password\":\""+PASSWORD+"\"}"))
            .andExpect(status().isOk()).andExpect(jsonPath("$.tokenType").value("Bearer"))
            .andReturn().getResponse().getContentAsString();
        return mapper.readTree(body).get("accessToken").asText();
    }

    @Test void sinTokenEs401() throws Exception {
        mvc.perform(get("/api/productos")).andExpect(status().isUnauthorized()).andExpect(jsonPath("$.status").value(401));
    }
    @Test void credencialesIncorrectasEs401() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content("{\"username\":\"admin\",\"password\":\"incorrecta\"}"))
            .andExpect(status().isUnauthorized()).andExpect(jsonPath("$.mensaje").value("Usuario o contrasena incorrectos."));
    }
    @Test void loginIncompletoEs400() throws Exception {
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isBadRequest());
    }
    @Test void tokenValidoInformaRolYSinHash() throws Exception {
        mvc.perform(get("/api/auth/me").header("Authorization","Bearer "+token("admin")))
            .andExpect(status().isOk()).andExpect(jsonPath("$.roles[0]").value("ADMIN"))
            .andExpect(jsonPath("$.roles.length()").value(1))
            .andExpect(jsonPath("$.passwordHash").doesNotExist());
        assertNotEquals(PASSWORD,usuarios.findByUsername("admin").orElseThrow().getPasswordHash());
        assertTrue(encoder.matches(PASSWORD,usuarios.findByUsername("admin").orElseThrow().getPasswordHash()));
    }
    @Test void vendedorConsultaPeroNoCreaProductos() throws Exception {
        String t=token("vendedor");
        mvc.perform(get("/api/productos").header("Authorization","Bearer "+t)).andExpect(status().isOk());
        mvc.perform(post("/api/productos").header("Authorization","Bearer "+t)
            .contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
    }
    @Test void vendedorCreaClientePeroNoLoElimina() throws Exception {
        String t=token("vendedor");
        String res=mvc.perform(post("/api/clientes").header("Authorization","Bearer "+t)
            .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\"Cliente JWT\"}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id=mapper.readTree(res).get("idCliente").asLong();
        mvc.perform(delete("/api/clientes/"+id).header("Authorization","Bearer "+t)).andExpect(status().isForbidden());
        assertTrue(clientes.existsById(id));
    }
    @Test void tokenAlteradoEs401() throws Exception {
        String t=token("admin");String[] parts=t.split("\\.");
        parts[1]="eyJzdWIiOiJvdHJvIn0";
        mvc.perform(get("/api/productos").header("Authorization","Bearer "+String.join(".",parts)))
            .andExpect(status().isUnauthorized());
    }
    @ParameterizedTest @ValueSource(strings={"vencido","emisor","audiencia"})
    void rechazaClaimsInvalidos(String tipo) throws Exception {
        Instant now=Instant.now();
        var claims=JwtClaimsSet.builder().issuer(tipo.equals("emisor")?"otro":"ecocompra-tech").subject("admin")
            .audience(List.of(tipo.equals("audiencia")?"otra-api":"ecocompra-api"))
            .issuedAt(now.minusSeconds(7200)).expiresAt(tipo.equals("vencido")?now.minusSeconds(3600):now.plusSeconds(1800))
            .claim("roles",List.of("ADMIN"))
            .claim("uid",usuarios.findByUsername("admin").orElseThrow().getId())
            .claim("version",0L).build();
        String t=jwtEncoder.encode(JwtEncoderParameters.from(JwsHeader.with(MacAlgorithm.HS256).build(),claims)).getTokenValue();
        mvc.perform(get("/api/productos").header("Authorization","Bearer "+t)).andExpect(status().isUnauthorized());
    }
    @Test void corsPermiteAngular() throws Exception {
        mvc.perform(options("/api/clientes").header("Origin","http://localhost:4200")
            .header("Access-Control-Request-Method","POST").header("Access-Control-Request-Headers","authorization,content-type"))
            .andExpect(status().isOk()).andExpect(header().string("Access-Control-Allow-Origin","http://localhost:4200"));
    }
    @Test void corsRechazaOtroOrigen() throws Exception {
        mvc.perform(options("/api/clientes").header("Origin","https://no-autorizado.example")
            .header("Access-Control-Request-Method","POST"))
            .andExpect(status().isForbidden()).andExpect(header().doesNotExist("Access-Control-Allow-Origin"));
    }
    @Test void adminRecibe400Y404() throws Exception {
        String t=token("admin");
        mvc.perform(post("/api/clientes").header("Authorization","Bearer "+t)
            .contentType(MediaType.APPLICATION_JSON).content("{\"nombre\":\" \",\"correo\":\"no-es-correo\"}"))
            .andExpect(status().isBadRequest()).andExpect(jsonPath("$.validaciones.nombre").exists());
        mvc.perform(get("/api/clientes/999999").header("Authorization","Bearer "+t)).andExpect(status().isNotFound());
    }
    @Test void ventaSeActualizaYEliminaConStockAtomico() throws Exception {
        var cat=new com.ecocompra.tech.entity.Categoria();cat.setNombre("Prueba");cat=categorias.saveAndFlush(cat);
        var p=new com.ecocompra.tech.entity.Producto();p.setNombre("Equipo");p.setCategoria(cat);
        p.setPrecio(new java.math.BigDecimal("10.00"));p.setStock(10);p=productos.saveAndFlush(p);
        var c=new com.ecocompra.tech.entity.Cliente();c.setNombre("Cliente");c=clientes.saveAndFlush(c);
        String a=token("admin"),v=token("vendedor");
        String base="{\"idCliente\":"+c.getIdCliente()+",\"detalles\":[{\"idProducto\":"+p.getIdProducto()+",\"cantidad\":";
        String res=mvc.perform(post("/api/ventas").header("Authorization","Bearer "+v)
            .contentType(MediaType.APPLICATION_JSON).content(base+"2}]}"))
            .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        long id=mapper.readTree(res).get("idVenta").asLong();
        mvc.perform(put("/api/ventas/"+id).header("Authorization","Bearer "+a)
            .contentType(MediaType.APPLICATION_JSON).content(base+"3}]}"))
            .andExpect(status().isOk());
        assertEquals(7,productos.findById(p.getIdProducto()).orElseThrow().getStock());
        assertEquals(1,detalles.findByVentaIdVenta(id).size());
        assertEquals(3,detalles.findByVentaIdVenta(id).getFirst().getCantidad());
        mvc.perform(put("/api/ventas/"+id).header("Authorization","Bearer "+a)
            .contentType(MediaType.APPLICATION_JSON).content(base+"100}]}"))
            .andExpect(status().isConflict());
        assertEquals(7,productos.findById(p.getIdProducto()).orElseThrow().getStock());
        mvc.perform(delete("/api/ventas/"+id).header("Authorization","Bearer "+v)).andExpect(status().isForbidden());
        mvc.perform(delete("/api/ventas/"+id).header("Authorization","Bearer "+a)).andExpect(status().isNoContent());
        assertEquals(10,productos.findById(p.getIdProducto()).orElseThrow().getStock());
        assertFalse(ventas.existsById(id));
        assertEquals(0,detalles.count());
    }
    @Test void detalleNuloEs400() throws Exception {
        mvc.perform(post("/api/ventas").header("Authorization","Bearer "+token("admin"))
            .contentType(MediaType.APPLICATION_JSON).content("{\"idCliente\":1,\"detalles\":[null]}"))
            .andExpect(status().isBadRequest());
    }
}
