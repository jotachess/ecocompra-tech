package com.ecocompra.tech.controller;

import java.math.BigDecimal;
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

import com.ecocompra.tech.dto.ProductoDto;
import com.ecocompra.tech.model.Producto;
import com.ecocompra.tech.service.ProductoService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/productos")
@RequiredArgsConstructor
public class ProductoController {

    private final ProductoService productoService;

    // --- Operaciones CRUD estándar ---

    @GetMapping
    public ResponseEntity<List<Producto>> listarTodos() {
        return ResponseEntity.ok(productoService.listarTodos());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Producto> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(productoService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Producto> crear(@Valid @RequestBody ProductoDto dto) {
        Producto nuevo = productoService.crear(dto);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nuevo.getIdProducto())
                .toUri();
        return ResponseEntity.created(ubicacion).body(nuevo);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Producto> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody ProductoDto dto) {
        return ResponseEntity.ok(productoService.actualizar(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        productoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // --- Endpoints con consultas JPQL ---

    @GetMapping("/buscar/nombre")
    public ResponseEntity<List<Producto>> buscarPorNombre(
            @RequestParam(required = false, defaultValue = "") String nombre) {
        return ResponseEntity.ok(productoService.buscarPorNombreJPQL(nombre));
    }

    @GetMapping("/buscar/consumo")
    public ResponseEntity<List<Producto>> buscarPorConsumoMaximo(
            @RequestParam BigDecimal max) {
        return ResponseEntity.ok(productoService.buscarPorConsumoMaximoJPQL(max));
    }

    @GetMapping("/buscar/categoria/{idCategoria}")
    public ResponseEntity<List<Producto>> buscarPorCategoria(
            @PathVariable Long idCategoria) {
        return ResponseEntity.ok(productoService.buscarPorCategoriaJPQL(idCategoria));
    }

    @GetMapping("/buscar/vida-util")
    public ResponseEntity<List<Producto>> buscarPorVidaUtilMinima(
            @RequestParam BigDecimal min) {
        return ResponseEntity.ok(productoService.buscarPorVidaUtilMinimaJPQL(min));
    }

    // --- Endpoints con Named Queries ---

    @GetMapping("/buscar/precio")
    public ResponseEntity<List<Producto>> buscarPorRangoPrecio(
            @RequestParam BigDecimal min,
            @RequestParam BigDecimal max) {
        return ResponseEntity.ok(productoService.buscarPorRangoPrecio(min, max));
    }

    @GetMapping("/buscar/categoria-stock")
    public ResponseEntity<List<Producto>> buscarPorCategoriaYStock(
            @RequestParam Long idCategoria,
            @RequestParam(defaultValue = "0") Integer stockMin) {
        return ResponseEntity.ok(productoService.buscarPorCategoriaYStock(idCategoria, stockMin));
    }

    @GetMapping("/buscar/reparabilidad")
    public ResponseEntity<List<Producto>> buscarPorReparabilidad(
            @RequestParam(required = false, defaultValue = "") String criterio) {
        return ResponseEntity.ok(productoService.buscarPorReparabilidadContiene(criterio));
    }
}
