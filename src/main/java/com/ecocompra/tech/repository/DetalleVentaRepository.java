package com.ecocompra.tech.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.ecocompra.tech.entity.DetalleVenta;

@Repository
public interface DetalleVentaRepository extends JpaRepository<DetalleVenta, Long> {

    List<DetalleVenta> findByVentaIdVenta(Long idVenta);

    List<DetalleVenta> findByProductoIdProducto(Long idProducto);

    long countByProductoIdProducto(Long idProducto);
}
