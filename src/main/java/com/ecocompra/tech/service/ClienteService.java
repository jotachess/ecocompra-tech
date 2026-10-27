package com.ecocompra.tech.service;

import java.util.List;

import com.ecocompra.tech.dto.ClienteDto;
import com.ecocompra.tech.model.Cliente;

public interface ClienteService {

    // --- Operaciones CRUD estándar ---

    List<Cliente> listarTodos();

    Cliente obtenerPorId(Long id);

    Cliente crear(ClienteDto dto);

    Cliente actualizar(Long id, ClienteDto dto);

    void eliminar(Long id);

    // --- Consultas JPQL con parámetros seguros ---

    List<Cliente> buscarPorTelefonoJPQL(String telefono);

    List<Cliente> buscarPorDominioCorreoJPQL(String dominio);

    // --- Consultas Named Queries con parámetros seguros ---

    Cliente buscarPorCorreo(String correo);

    List<Cliente> buscarPorNombreContiene(String termino);
}
