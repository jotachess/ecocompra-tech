package com.ecocompra.tech.security;

import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.access.AccessDeniedException;
import com.ecocompra.tech.exception.*;
import lombok.RequiredArgsConstructor;

@Service @RequiredArgsConstructor @Transactional
public class UsuarioService {
    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    @Transactional(readOnly=true)
    public List<UsuarioDtos.Respuesta> listar() {
        return usuarios.findAll().stream().map(UsuarioDtos.Respuesta::de).toList();
    }
    @Transactional(readOnly=true)
    public UsuarioDtos.Respuesta obtener(Long id) { return UsuarioDtos.Respuesta.de(buscar(id)); }
    private Usuario buscar(Long id) {
        return usuarios.findById(id).orElseThrow(()->new ResourceNotFoundException("Usuario","id",id));
    }
    public UsuarioDtos.Respuesta crear(UsuarioDtos.Crear dto) {
        if(usuarios.findByUsernameIgnoreCase(dto.username()).isPresent())
            throw new IllegalOperationException("Ya existe un usuario con ese nombre.");
        Usuario u=new Usuario();u.setUsername(dto.username());u.setRol(dto.rol());
        u.setPasswordHash(encoder.encode(dto.password()));
        return UsuarioDtos.Respuesta.de(usuarios.saveAndFlush(u));
    }
    private void comprobarCambio(List<Usuario> todos,Usuario u,Rol rol,boolean activo,String actor) {
        if(todos.stream().noneMatch(a->a.getUsername().equals(actor)&&a.isActivo()&&a.getRol()==Rol.ADMIN))
            throw new AccessDeniedException("La cuenta administradora ya no esta disponible.");
        if(u.getUsername().equals(actor)&&(!activo||rol!=Rol.ADMIN))
            throw new IllegalOperationException("No puede desactivar, eliminar ni quitar el rol ADMIN de su propia cuenta.");
        if(u.isActivo()&&u.getRol()==Rol.ADMIN&&(!activo||rol!=Rol.ADMIN)
                &&todos.stream().filter(a->a.isActivo()&&a.getRol()==Rol.ADMIN).count()<=1)
            throw new IllegalOperationException("Debe conservar al menos un administrador activo.");
    }
    public UsuarioDtos.Respuesta actualizar(Long id,UsuarioDtos.Actualizar dto,String actor) {
        var todos=usuarios.bloquearTodos();Usuario u=buscar(id);
        comprobarCambio(todos,u,dto.rol(),dto.activo(),actor);
        if(u.getRol()!=dto.rol()||u.isActivo()!=dto.activo())u.setTokenVersion(u.getTokenVersion()+1);
        u.setRol(dto.rol());u.setActivo(dto.activo());
        return UsuarioDtos.Respuesta.de(usuarios.saveAndFlush(u));
    }
    public void eliminar(Long id,String actor) {
        var todos=usuarios.bloquearTodos();Usuario u=buscar(id);
        comprobarCambio(todos,u,u.getRol(),false,actor);
        usuarios.delete(u);usuarios.flush();
    }
}
