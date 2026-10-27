package com.ecocompra.tech.service;

import java.math.BigDecimal;
import java.util.List;

import com.ecocompra.tech.dto.ProductoDto;
import com.ecocompra.tech.model.Producto;

public interface ProductoService {

    // --- Operaciones CRUD estándar ---

    List<Producto> listarTodos();

    Producto obtenerPorId(Long id);

    Producto crear(ProductoDto dto);

    Producto actualizar(Long id, ProductoDto dto);

    void eliminar(Long id);

    // --- Consultas JPQL con parámetros seguros ---

    List<Producto> buscarPorNombreJPQL(String nombre);

    List<Producto> buscarPorConsumoMaximoJPQL(BigDecimal consumoMax);

    List<Producto> buscarPorCategoriaJPQL(Long idCategoria);

    List<Producto> buscarPorVidaUtilMinimaJPQL(BigDecimal vidaUtilMin);

    // --- Consultas Named Queries con parámetros seguros ---

    List<Producto> buscarPorRangoPrecio(BigDecimal precioMin, BigDecimal precioMax);

    List<Producto> buscarPorCategoriaYStock(Long idCategoria, Integer stockMin);

    List<Producto> buscarPorReparabilidadContiene(String criterio);
}
