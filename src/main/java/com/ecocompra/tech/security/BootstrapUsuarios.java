package com.ecocompra.tech.security;

import org.springframework.boot.CommandLineRunner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Alta inicial controlada por variables de entorno; no existe registro publico. */
@Component
public class BootstrapUsuarios implements CommandLineRunner {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String adminPassword, vendedorPassword;
    public BootstrapUsuarios(UsuarioRepository usuarios, PasswordEncoder encoder,
            @Value("${security.bootstrap.admin-password:}") String adminPassword,
            @Value("${security.bootstrap.vendedor-password:}") String vendedorPassword) {
        this.usuarios=usuarios; this.encoder=encoder;
        this.adminPassword=adminPassword; this.vendedorPassword=vendedorPassword;
    }
    @Override public void run(String... args) {
        crear("admin", adminPassword, Rol.ADMIN);
        crear("vendedor", vendedorPassword, Rol.VENDEDOR);
    }
    private void crear(String nombre, String password, Rol rol) {
        if (password.isBlank() || usuarios.findByUsername(nombre).isPresent()) return;
        int bytes=password.getBytes(java.nio.charset.StandardCharsets.UTF_8).length;
        if (bytes<12 || bytes>72) throw new IllegalStateException("La clave inicial debe tener entre 12 y 72 bytes");
        Usuario u=new Usuario(); u.setUsername(nombre); u.setPasswordHash(encoder.encode(password)); u.setRol(rol);
        usuarios.save(u);
    }
}
