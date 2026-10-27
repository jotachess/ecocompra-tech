
package com.ecocompra.tech.controller;

import com.ecocompra.tech.entity.Producto;
import com.ecocompra.tech.repository.ProductoRepository;
import com.ecocompra.tech.repository.CategoriaRepository;
import com.ecocompra.tech.entity.Categoria;
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
    private final CategoriaRepository categorias;

    public ProductoController(ProductoRepository repository, CategoriaRepository categorias) {
        this.repository = repository;
        this.categorias = categorias;
    }

    // CREAR PRODUCTO
    @PostMapping
    public ResponseEntity<Producto> crear(
            @Valid @RequestBody Producto producto) {
        if (producto.getIdProducto() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "No se permite enviar un ID al crear un producto");
        }
        producto.setCategoria(resolverCategoria(producto.getCategoria()));
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
        actual.setCategoria(resolverCategoria(datos.getCategoria()));
        actual.setPrecio(datos.getPrecio());
        actual.setStock(datos.getStock());
        actual.setReparabilidad(datos.getReparabilidad());
        actual.setConsumoKwh(datos.getConsumoKwh());
        actual.setCondicionesConsumo(datos.getCondicionesConsumo());
        actual.setVidaUtilAnios(datos.getVidaUtilAnios());
        actual.setFuenteTecnica(datos.getFuenteTecnica());

        Producto guardado = repository.save(actual);
        // Recargar con la categoría antes de serializar fuera de la transacción.
        return buscar(guardado.getIdProducto());
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

    private Categoria resolverCategoria(Categoria categoria) {
        if (categoria == null || categoria.getIdCategoria() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Se requiere el ID de una categoría existente");
        }
        return categorias.findById(categoria.getIdCategoria())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Categoría no encontrada"));
    }
}
