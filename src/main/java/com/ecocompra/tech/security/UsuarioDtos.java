package com.ecocompra.tech.security;

import java.nio.charset.StandardCharsets;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;

public final class UsuarioDtos {
    private UsuarioDtos() {}
    public record Crear(
        @NotBlank @Pattern(regexp="[a-z0-9][a-z0-9._-]{2,79}", message="Use de 3 a 80 letras minusculas, numeros, punto, guion o guion bajo") String username,
        @NotBlank @Size(min=12,max=72,message="La clave debe tener entre 12 y 72 caracteres") String password,
        @NotNull(message="Seleccione un rol") Rol rol) {
        @JsonIgnore @AssertTrue(message="La clave no puede superar 72 bytes UTF-8")
        public boolean isPasswordBytesValidos() {
            return password==null || password.getBytes(StandardCharsets.UTF_8).length<=72;
        }
    }
    public record Actualizar(@NotNull Rol rol, @NotNull Boolean activo) {}
    public record Respuesta(Long id,String username,Rol rol,boolean activo) {
        static Respuesta de(Usuario u) { return new Respuesta(u.getId(),u.getUsername(),u.getRol(),u.isActivo()); }
    }
}
