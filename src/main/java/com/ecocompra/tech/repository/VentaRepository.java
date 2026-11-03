package com.ecocompra.tech.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecocompra.tech.entity.Venta;

@Repository
public interface VentaRepository extends JpaRepository<Venta, Long> {
    @org.springframework.data.jpa.repository.Lock(jakarta.persistence.LockModeType.PESSIMISTIC_WRITE)
    @Query("select v from Venta v where v.idVenta = :id")
    java.util.Optional<Venta> bloquearPorId(@Param("id") Long id);

    List<Venta> findByClienteIdCliente(Long idCliente);

    long countByClienteIdCliente(Long idCliente);

    @Query("SELECT v FROM Venta v WHERE v.fecha BETWEEN :desde AND :hasta ORDER BY v.fecha DESC")
    List<Venta> buscarPorRangoFechasJPQL(@Param("desde") LocalDateTime desde, @Param("hasta") LocalDateTime hasta);
}
