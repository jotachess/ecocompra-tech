package com.ecocompra.tech.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ecocompra.tech.dto.ClienteDto;
import com.ecocompra.tech.model.Cliente;
import com.ecocompra.tech.service.ClienteService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/clientes")
@RequiredArgsConstructor
public class ClienteController {

    private final ClienteService clienteService;

    // --- Operaciones CRUD estándar ---

    @GetMapping
    public ResponseEntity<List<Cliente>> listarTodos() {
        return ResponseEntity.ok(clienteService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Cliente> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(clienteService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Cliente> crear(@Valid @RequestBody ClienteDto dto) {
        Cliente nuevo = clienteService.crear(dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nuevo.getIdCliente())
                .toUri();
        return ResponseEntity.created(ubicacion).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cliente> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ClienteDto dto) {
        return ResponseEntity.ok(clienteService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        clienteService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints con consultas JPQL ---

    @GetMapping("/buscar/telefono")
    public ResponseEntity<List<Cliente>> buscarPorTelefono(
            @RequestParam String telefono) {
        return ResponseEntity.ok(clienteService.buscarPorTelefonoJPQL(telefono));
    }

    @GetMapping("/buscar/dominio")
    public ResponseEntity<List<Cliente>> buscarPorDominioCorreo(
            @RequestParam String dominio) {
        return ResponseEntity.ok(clienteService.buscarPorDominioCorreoJPQL(dominio));
    }

    // --- Endpoints con Named Queries ---

    @GetMapping("/buscar/correo")
    public ResponseEntity<Cliente> buscarPorCorreo(
            @RequestParam String correo) {
        return ResponseEntity.ok(clienteService.buscarPorCorreo(correo));
    }

    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<Cliente>> buscarPorNombre(
            @RequestParam(required = false, defaultValue = "") String termino) {
        return ResponseEntity.ok(clienteService.buscarPorNombreContiene(termino));
    }
}
