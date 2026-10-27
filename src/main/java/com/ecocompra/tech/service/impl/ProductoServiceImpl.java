package com.ecocompra.tech.service.impl;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecocompra.tech.dto.ProductoDto;
import com.ecocompra.tech.exception.IllegalOperationException;
import com.ecocompra.tech.exception.ResourceNotFoundException;
import com.ecocompra.tech.model.Categoria;
import com.ecocompra.tech.model.Producto;
import com.ecocompra.tech.repository.CategoriaRepository;
import com.ecocompra.tech.repository.DetalleVentaRepository;
import com.ecocompra.tech.repository.ProductoRepository;
import com.ecocompra.tech.service.ProductoService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductoServiceImpl implements ProductoService {

    private final ProductoRepository productoRepository;
    private final CategoriaRepository categoriaRepository;
    private final DetalleVentaRepository detalleVentaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Producto> listarTodos() {
        return productoRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Producto obtenerPorId(Long id) {
        return productoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", id));
    }

    @Override
    public Producto crear(ProductoDto dto) {
        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", dto.getIdCategoria()));

        validarReglasSostenibilidad(dto);

        Producto producto = Producto.builder()
                .categoria(categoria)
                .nombre(dto.getNombre().trim())
                .descripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null)
                .precio(dto.getPrecio())
                .stock(dto.getStock())
                .reparabilidad(dto.getReparabilidad() != null ? dto.getReparabilidad().trim() : null)
                .consumoKwh(dto.getConsumoKwh())
                .condicionesConsumo(dto.getCondicionesConsumo() != null ? dto.getCondicionesConsumo().trim() : null)
                .vidaUtilAnios(dto.getVidaUtilAnios())
                .fuenteTecnica(dto.getFuenteTecnica() != null ? dto.getFuenteTecnica().trim() : null)
                .build();

        return productoRepository.save(producto);
    }

    @Override
    public Producto actualizar(Long id, ProductoDto dto) {
        Producto producto = obtenerPorId(id);

        Categoria categoria = categoriaRepository.findById(dto.getIdCategoria())
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", dto.getIdCategoria()));

        validarReglasSostenibilidad(dto);

        producto.setCategoria(categoria);
        producto.setNombre(dto.getNombre().trim());
        producto.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);
        producto.setPrecio(dto.getPrecio());
        producto.setStock(dto.getStock());
        producto.setReparabilidad(dto.getReparabilidad() != null ? dto.getReparabilidad().trim() : null);
        producto.setConsumoKwh(dto.getConsumoKwh());
        producto.setCondicionesConsumo(dto.getCondicionesConsumo() != null ? dto.getCondicionesConsumo().trim() : null);
        producto.setVidaUtilAnios(dto.getVidaUtilAnios());
        producto.setFuenteTecnica(dto.getFuenteTecnica() != null ? dto.getFuenteTecnica().trim() : null);

        return productoRepository.save(producto);
    }

    @Override
    public void eliminar(Long id) {
        Producto producto = obtenerPorId(id);

        long ventasAsociadas = detalleVentaRepository.countByProductoIdProducto(id);
        if (ventasAsociadas > 0) {
            throw new IllegalOperationException("No se puede eliminar el producto con ID " + id
                    + " porque registra " + ventasAsociadas + " detalle(s) de ventas asociadas (Regla ON DELETE RESTRICT).");
        }

        productoRepository.delete(producto);
    }

    // --- Consultas JPQL con parámetros enlazados ---

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorNombreJPQL(String nombre) {
        if (nombre == null || nombre.isBlank()) {
            return listarTodos();
        }
        return productoRepository.buscarPorNombreJPQL(nombre.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorConsumoMaximoJPQL(BigDecimal consumoMax) {
        if (consumoMax == null || consumoMax.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("El consumo máximo debe ser un valor positivo");
        }
        return productoRepository.buscarPorConsumoMaximoJPQL(consumoMax);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoriaJPQL(Long idCategoria) {
        if (!categoriaRepository.existsById(idCategoria)) {
            throw new ResourceNotFoundException("Categoría", "id", idCategoria);
        }
        return productoRepository.buscarPorCategoriaJPQL(idCategoria);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorVidaUtilMinimaJPQL(BigDecimal vidaUtilMin) {
        if (vidaUtilMin == null || vidaUtilMin.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("La vida útil mínima debe ser mayor a cero");
        }
        return productoRepository.buscarPorVidaUtilMinimaJPQL(vidaUtilMin);
    }

    // --- Named Queries con parámetros enlazados ---

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax) {
        if (precioMin == null || precioMax == null) {
            throw new IllegalArgumentException("Los límites de precio mínimo y máximo son requeridos");
        }
        if (precioMin.compareTo(precioMax) > 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor que el precio máximo");
        }
        return productoRepository.buscarPorRangoPrecio(precioMin, precioMax);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorCategoriaYStock(Long idCategoria, Integer stockMin) {
        if (stockMin == null || stockMin < 0) {
            throw new IllegalArgumentException("El stock mínimo debe ser mayor o igual a cero");
        }
        return productoRepository.buscarPorCategoriaYStock(idCategoria, stockMin);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Producto> buscarPorReparabilidadContiene(String criterio) {
        if (criterio == null || criterio.isBlank()) {
            return listarTodos();
        }
        return productoRepository.buscarPorReparabilidadContiene(criterio.trim());
    }

    // --- Reglas de validación de negocio sostenible ---

    private void validarReglasSostenibilidad(ProductoDto dto) {
        // Regla: si se indica consumo, las condiciones de consumo son obligatorias
        if (dto.getConsumoKwh() != null && (dto.getCondicionesConsumo() == null || dto.getCondicionesConsumo().isBlank())) {
            throw new IllegalOperationException(
                    "Si se registra consumo en kWh, es obligatorio especificar las condiciones de consumo (horas, modo de uso, etc.).");
        }

        // Regla: si se informa reparabilidad, consumo o vida util, la fuente tecnica es obligatoria
        boolean tieneDatosSostenibles = dto.getConsumoKwh() != null
                || (dto.getReparabilidad() != null && !dto.getReparabilidad().isBlank())
                || dto.getVidaUtilAnios() != null;

        if (tieneDatosSostenibles && (dto.getFuenteTecnica() == null || dto.getFuenteTecnica().isBlank())) {
            throw new IllegalOperationException(
                    "Al registrar métricas sostenibles (consumo, reparabilidad o vida útil), es obligatorio indicar una fuente técnica verificable.");
        }
    }
}
