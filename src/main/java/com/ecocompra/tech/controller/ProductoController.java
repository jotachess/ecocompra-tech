
package com.ecocompra.tech.controller;

import com.ecocompra.tech.entity.Producto;
import com.ecocompra.tech.repository.ProductoRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.dao.DataIntegrityViolationException;
import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoRepository repository;

    public ProductoController(ProductoRepository repository) {
        this.repository = repository;
    }

    // CREAR PRODUCTO
    @PostMapping
    public ResponseEntity<Producto> crear(
            @Valid @RequestBody Producto producto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(repository.save(producto));
    }

    // LISTAR PRODUCTOS
    @GetMapping
    public List<Producto> listar() {
        return repository.findAll();
    }

    // BUSCAR PRODUCTO POR ID
    @GetMapping("/{id}")
    public Producto buscar(@PathVariable Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Producto no encontrado"));
    }

    // ACTUALIZAR PRODUCTO
    @PutMapping("/{id}")
    public Producto actualizar(
            @PathVariable Long id,
            @Valid @RequestBody Producto datos) {

        Producto actual = buscar(id);

        actual.setNombre(datos.getNombre());
        actual.setDescripcion(datos.getDescripcion());
        actual.setCategoria(datos.getCategoria());
        actual.setPrecio(datos.getPrecio());
        actual.setStock(datos.getStock());
        actual.setReparabilidad(datos.getReparabilidad());
        actual.setConsumoKwh(datos.getConsumoKwh());
        actual.setCondicionesConsumo(datos.getCondicionesConsumo());
        actual.setVidaUtilAnios(datos.getVidaUtilAnios());
        actual.setFuenteTecnica(datos.getFuenteTecnica());

        return repository.save(actual);
    }

    // ELIMINAR PRODUCTO
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {

        Producto producto = buscar(id);

        try {
            repository.delete(producto);
            repository.flush();
        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Producto vinculado a una venta");
        }

        return ResponseEntity.noContent().build();
    }
}
