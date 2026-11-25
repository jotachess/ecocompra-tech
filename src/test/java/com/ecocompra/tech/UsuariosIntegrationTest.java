package com.ecocompra.tech;

import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import com.ecocompra.tech.security.*;
import tools.jackson.databind.ObjectMapper;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest @AutoConfigureMockMvc
class UsuariosIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired UsuarioRepository usuarios;
    @Autowired PasswordEncoder encoder;
    @Autowired ObjectMapper mapper;
    private String admin,seller;
    private static final String PASSWORD="ClavePruebaLocal05!";
    @BeforeEach void preparar() throws Exception {
        usuarios.deleteAll();
        crearCuenta("admin",Rol.ADMIN);crearCuenta("vendedor",Rol.VENDEDOR);
        admin=login("admin");seller=login("vendedor");
    }
    Usuario crearCuenta(String name,Rol rol) {
        Usuario u=new Usuario();u.setUsername(name);u.setRol(rol);u.setPasswordHash(encoder.encode(PASSWORD));return usuarios.saveAndFlush(u);
    }
    String login(String name) throws Exception {
        var r=mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(java.util.Map.of("username",name,"password",PASSWORD))))
            .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        return mapper.readTree(r).get("accessToken").asText();
    }
    String createBody(String name,String password,String rol) throws Exception {
        return mapper.writeValueAsString(java.util.Map.of("username",name,"password",password,"rol",rol));
    }
    @Test void listadoAdminSinExponerClaves() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization","Bearer "+admin)).andExpect(status().isOk())
            .andExpect(jsonPath("$[0].passwordHash").doesNotExist()).andExpect(jsonPath("$[0].password").doesNotExist())
            .andExpect(jsonPath("$[0].tokenVersion").doesNotExist());
    }
    @Test void usuarioSinTokenNoAccede() throws Exception { mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized()); }
    @Test void vendedorNoListaNiConsultaCuentas() throws Exception {
        mvc.perform(get("/api/usuarios").header("Authorization","Bearer "+seller)).andExpect(status().isForbidden());
        mvc.perform(get("/api/usuarios/1").header("Authorization","Bearer "+seller)).andExpect(status().isForbidden());
    }
    @Test void vendedorNoCreaActualizaNiEliminaCuentas() throws Exception {
        mvc.perform(post("/api/usuarios").header("Authorization","Bearer "+seller).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
        mvc.perform(put("/api/usuarios/1").header("Authorization","Bearer "+seller).contentType(MediaType.APPLICATION_JSON).content("{}"))
            .andExpect(status().isForbidden());
        mvc.perform(delete("/api/usuarios/1").header("Authorization","Bearer "+seller)).andExpect(status().isForbidden());
    }
    @Test void crearGuardaHashBcryptYPermiteLogin() throws Exception {
        mvc.perform(post("/api/usuarios").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
            .content(createBody("nuevo05",PASSWORD,"VENDEDOR"))).andExpect(status().isCreated())
            .andExpect(header().exists("Location")).andExpect(jsonPath("$.passwordHash").doesNotExist());
        Usuario u=usuarios.findByUsername("nuevo05").orElseThrow();assertTrue(encoder.matches(PASSWORD,u.getPasswordHash()));
        assertNotEquals(PASSWORD,u.getPasswordHash());assertNotNull(login("nuevo05"));
    }
    @Test void duplicadoEs409YNoAgregaFila() throws Exception {
        long count=usuarios.count();
        mvc.perform(post("/api/usuarios").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
            .content(createBody("vendedor",PASSWORD,"ADMIN"))).andExpect(status().isConflict());
        assertEquals(count,usuarios.count());
    }
    @Test void claveCortaEs400() throws Exception {
        mvc.perform(post("/api/usuarios").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
            .content(createBody("nuevo05","corta","VENDEDOR"))).andExpect(status().isBadRequest());
        assertTrue(usuarios.findByUsername("nuevo05").isEmpty());
    }
    @Test void claveUnicodeDeMasDe72BytesEs400() throws Exception {
        mvc.perform(post("/api/usuarios").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
            .content(createBody("nuevo05","á".repeat(40),"VENDEDOR"))).andExpect(status().isBadRequest());
    }
    @Test void nombreInvalidoEs400() throws Exception {
        mvc.perform(post("/api/usuarios").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
            .content(createBody("   ",PASSWORD,"VENDEDOR"))).andExpect(status().isBadRequest());
    }
    @Test void rolInvalidoEs400() throws Exception {
        mvc.perform(post("/api/usuarios").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
            .content(createBody("nuevo05",PASSWORD,"OTRO"))).andExpect(status().isBadRequest());
    }
    @Test void editarRolRevocaTokenAnterior() throws Exception {
        Usuario u=usuarios.findByUsername("vendedor").orElseThrow();
        mvc.perform(put("/api/usuarios/"+u.getId()).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"rol\":\"ADMIN\",\"activo\":true}")).andExpect(status().isOk());
        mvc.perform(get("/api/productos").header("Authorization","Bearer "+seller)).andExpect(status().isUnauthorized());
        mvc.perform(get("/api/usuarios").header("Authorization","Bearer "+login("vendedor"))).andExpect(status().isOk());
    }
    @Test void desactivarRevocaYNoPermiteNuevoLogin() throws Exception {
        Usuario u=usuarios.findByUsername("vendedor").orElseThrow();
        mvc.perform(put("/api/usuarios/"+u.getId()).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"rol\":\"VENDEDOR\",\"activo\":false}")).andExpect(status().isOk());
        mvc.perform(get("/api/productos").header("Authorization","Bearer "+seller)).andExpect(status().isUnauthorized());
        mvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
            .content(mapper.writeValueAsString(java.util.Map.of("username","vendedor","password",PASSWORD))))
            .andExpect(status().isUnauthorized());
    }
    @Test void reactivarNoReviveTokenAnterior() throws Exception {
        Usuario u=usuarios.findByUsername("vendedor").orElseThrow();
        for(boolean active:new boolean[]{false,true})mvc.perform(put("/api/usuarios/"+u.getId()).header("Authorization","Bearer "+admin)
            .contentType(MediaType.APPLICATION_JSON).content("{\"rol\":\"VENDEDOR\",\"activo\":"+active+"}")).andExpect(status().isOk());
        mvc.perform(get("/api/productos").header("Authorization","Bearer "+seller)).andExpect(status().isUnauthorized());
        assertNotNull(login("vendedor"));
    }
    @Test void noPermiteAutoDesactivacionNiAutoDegradacionNiAutoBorrado() throws Exception {
        long id=usuarios.findByUsername("admin").orElseThrow().getId();
        for(String body:new String[]{"{\"rol\":\"ADMIN\",\"activo\":false}","{\"rol\":\"VENDEDOR\",\"activo\":true}"})
            mvc.perform(put("/api/usuarios/"+id).header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/usuarios/"+id).header("Authorization","Bearer "+admin)).andExpect(status().isConflict());
        assertTrue(usuarios.findById(id).orElseThrow().isActivo());
    }
    @Test void eliminarEs204Luego404YRevocaToken() throws Exception {
        Usuario u=usuarios.findByUsername("vendedor").orElseThrow();
        mvc.perform(delete("/api/usuarios/"+u.getId()).header("Authorization","Bearer "+admin)).andExpect(status().isNoContent());
        mvc.perform(get("/api/usuarios/"+u.getId()).header("Authorization","Bearer "+admin)).andExpect(status().isNotFound());
        mvc.perform(get("/api/productos").header("Authorization","Bearer "+seller)).andExpect(status().isUnauthorized());
    }
    @Test void recrearNombreNoReutilizaTokenAnterior() throws Exception {
        Usuario u=usuarios.findByUsername("vendedor").orElseThrow();usuarios.delete(u);usuarios.flush();crearCuenta("vendedor",Rol.VENDEDOR);
        mvc.perform(get("/api/productos").header("Authorization","Bearer "+seller)).andExpect(status().isUnauthorized());
    }
    @Test void identificadorInexistenteEs404() throws Exception {
        mvc.perform(put("/api/usuarios/9999999").header("Authorization","Bearer "+admin).contentType(MediaType.APPLICATION_JSON)
            .content("{\"rol\":\"VENDEDOR\",\"activo\":true}")).andExpect(status().isNotFound());
    }
}
