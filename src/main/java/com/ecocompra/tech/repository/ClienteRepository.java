package com.ecocompra.tech.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.ecocompra.tech.entity.Cliente;

@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    // --- Consultas JPQL explícitas con parámetros enlazados seguros ---

    @Query("SELECT c FROM Cliente c WHERE c.telefono = :telefono")
    List<Cliente> buscarPorTelefonoJPQL(@Param("telefono") String telefono);

    @Query("SELECT c FROM Cliente c WHERE LOWER(c.correo) LIKE LOWER(CONCAT('%', :dominio))")
    List<Cliente> buscarPorDominioCorreoJPQL(@Param("dominio") String dominio);

    // --- Named Queries declaradas en la entidad Cliente con parámetros seguros ---

    Optional<Cliente> buscarPorCorreo(@Param("correo") String correo);

    List<Cliente> buscarPorNombreContiene(@Param("termino") String termino);

    // --- Métodos derivados ---

    boolean existsByCorreoIgnoreCase(String correo);
}
