package com.ecocompra.tech.repository;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecocompra.tech.model.Producto;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {

    // --- Consultas JPQL explícitas con parámetros enlazados seguros ---

    @Query("SELECT p FROM Producto p WHERE LOWER(p.nombre) LIKE LOWER(CONCAT('%', :nombre, '%')) ORDER BY p.nombre ASC")
    List<Producto> buscarPorNombreJPQL(@Param("nombre") String nombre);

    @Query("SELECT p FROM Producto p WHERE p.consumoKwh IS NOT NULL AND p.consumoKwh <= :consumoMax ORDER BY p.consumoKwh ASC")
    List<Producto> buscarPorConsumoMaximoJPQL(@Param("consumoMax") BigDecimal consumoMax);

    @Query("SELECT p FROM Producto p WHERE p.categoria.idCategoria = :idCategoria ORDER BY p.precio ASC")
    List<Producto> buscarPorCategoriaJPQL(@Param("idCategoria") Long idCategoria);

    @Query("SELECT p FROM Producto p WHERE p.vidaUtilAnios IS NOT NULL AND p.vidaUtilAnios >= :vidaUtilMin ORDER BY p.vidaUtilAnios DESC")
    List<Producto> buscarPorVidaUtilMinimaJPQL(@Param("vidaUtilMin") BigDecimal vidaUtilMin);

    // --- Named Queries declaradas en la entidad Producto con parámetros seguros ---

    List<Producto> buscarPorRangoPrecio(@Param("precioMin") BigDecimal precioMin, @Param("precioMax") BigDecimal precioMax);

    List<Producto> buscarPorCategoriaYStock(@Param("idCategoria") Long idCategoria, @Param("stockMin") Integer stockMin);

    List<Producto> buscarPorReparabilidadContiene(@Param("criterio") String criterio);

    // --- Métodos derivados ---

    List<Producto> findByCategoriaIdCategoria(Long idCategoria);

    boolean existsByNombreIgnoreCase(String nombre);
}
