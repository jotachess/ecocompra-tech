
package com.ecocompra.tech.controller;

import com.ecocompra.tech.entity.Cliente;
import com.ecocompra.tech.repository.ClienteRepository;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
public class ClienteController {

    private final ClienteRepository repository;

    public ClienteController(ClienteRepository repository) {
        this.repository = repository;
    }

    // CREAR CLIENTE
    @PostMapping
    public ResponseEntity<Cliente> crear(
            @Valid @RequestBody Cliente cliente) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repository.save(cliente));
    }

    // LISTAR CLIENTES
    @GetMapping
    public List<Cliente> listar() {
        return repository.findAll();
    }

    // BUSCAR CLIENTE POR ID
    @GetMapping("/{id}")
    public Cliente buscar(@PathVariable Long id) {

        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Cliente no encontrado"));
    }

    // ACTUALIZAR CLIENTE
    @PutMapping("/{id}")
    public Cliente actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Cliente datos) {

        Cliente actual = buscar(id);

        actual.setNombre(datos.getNombre());
        actual.setCorreo(datos.getCorreo());
        actual.setTelefono(datos.getTelefono());

        return repository.save(actual);
    }

    // ELIMINAR CLIENTE
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(
            @PathVariable Long id) {

        Cliente cliente = buscar(id);

        try {
            repository.delete(cliente);
            repository.flush();

        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Cliente vinculado a ventas");
        }

        return ResponseEntity.noContent().build();
    }
}
