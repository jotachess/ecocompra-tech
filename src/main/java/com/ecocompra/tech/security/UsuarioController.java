package com.ecocompra.tech.security;

import java.net.URI;
import java.util.List;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;
import lombok.RequiredArgsConstructor;

@RestController @RequestMapping("/api/usuarios") @RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService usuarios;
    @GetMapping public List<UsuarioDtos.Respuesta> index() { return usuarios.listar(); }
    @GetMapping("/{id}") public UsuarioDtos.Respuesta show(@PathVariable Long id) { return usuarios.obtener(id); }
    @PostMapping public ResponseEntity<UsuarioDtos.Respuesta> create(@Valid @RequestBody UsuarioDtos.Crear dto) {
        var u=usuarios.crear(dto);return ResponseEntity.created(URI.create("/api/usuarios/"+u.id())).body(u);
    }
    @PutMapping("/{id}") public UsuarioDtos.Respuesta update(@PathVariable Long id,
            @Valid @RequestBody UsuarioDtos.Actualizar dto,@AuthenticationPrincipal Jwt jwt) {
        return usuarios.actualizar(id,dto,jwt.getSubject());
    }
    @DeleteMapping("/{id}") public ResponseEntity<Void> delete(@PathVariable Long id,@AuthenticationPrincipal Jwt jwt) {
        usuarios.eliminar(id,jwt.getSubject());return ResponseEntity.noContent().build();
    }
}
