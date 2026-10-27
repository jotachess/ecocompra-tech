package com.ecocompra.tech.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecocompra.tech.dto.ClienteDto;
import com.ecocompra.tech.exception.IllegalOperationException;
import com.ecocompra.tech.exception.ResourceNotFoundException;
import com.ecocompra.tech.model.Cliente;
import com.ecocompra.tech.repository.ClienteRepository;
import com.ecocompra.tech.repository.VentaRepository;
import com.ecocompra.tech.service.ClienteService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ClienteServiceImpl implements ClienteService {

    private final ClienteRepository clienteRepository;
    private final VentaRepository ventaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> listarTodos() {
        return clienteRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Cliente obtenerPorId(Long id) {
        return clienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", id));
    }

    @Override
    public Cliente crear(ClienteDto dto) {
        if (dto.getCorreo() != null && !dto.getCorreo().isBlank()) {
            String correoLimpio = dto.getCorreo().trim();
            if (clienteRepository.existsByCorreoIgnoreCase(correoLimpio)) {
                throw new IllegalOperationException("Ya existe un cliente registrado con el correo: " + correoLimpio);
            }
        }

        Cliente cliente = Cliente.builder()
                .nombre(dto.getNombre().trim())
                .correo(dto.getCorreo() != null && !dto.getCorreo().isBlank() ? dto.getCorreo().trim() : null)
                .telefono(dto.getTelefono() != null && !dto.getTelefono().isBlank() ? dto.getTelefono().trim() : null)
                .build();

        return clienteRepository.save(cliente);
    }

    @Override
    public Cliente actualizar(Long id, ClienteDto dto) {
        Cliente cliente = obtenerPorId(id);

        if (dto.getCorreo() != null && !dto.getCorreo().isBlank()) {
            String correoLimpio = dto.getCorreo().trim();
            if (cliente.getCorreo() == null || !cliente.getCorreo().equalsIgnoreCase(correoLimpio)) {
                if (clienteRepository.existsByCorreoIgnoreCase(correoLimpio)) {
                    throw new IllegalOperationException("Ya existe otro cliente con el correo: " + correoLimpio);
                }
            }
            cliente.setCorreo(correoLimpio);
        } else {
            cliente.setCorreo(null);
        }

        cliente.setNombre(dto.getNombre().trim());
        cliente.setTelefono(dto.getTelefono() != null && !dto.getTelefono().isBlank() ? dto.getTelefono().trim() : null);

        return clienteRepository.save(cliente);
    }

    @Override
    public void eliminar(Long id) {
        Cliente cliente = obtenerPorId(id);

        long ventasAsociadas = ventaRepository.countByClienteIdCliente(id);
        if (ventasAsociadas > 0) {
            throw new IllegalOperationException("No se puede eliminar el cliente con ID " + id
                    + " porque tiene " + ventasAsociadas + " venta(s) registrada(s) (Regla ON DELETE RESTRICT).");
        }

        clienteRepository.delete(cliente);
    }

    // --- Consultas JPQL con parámetros enlazados seguros ---

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorTelefonoJPQL(String telefono) {
        if (telefono == null || telefono.isBlank()) {
            throw new IllegalArgumentException("El número de teléfono para la búsqueda es obligatorio");
        }
        return clienteRepository.buscarPorTelefonoJPQL(telefono.trim());
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorDominioCorreoJPQL(String dominio) {
        if (dominio == null || dominio.isBlank()) {
            throw new IllegalArgumentException("El dominio para la búsqueda es obligatorio");
        }
        return clienteRepository.buscarPorDominioCorreoJPQL(dominio.trim());
    }

    // --- Consultas Named Queries con parámetros enlazados seguros ---

    @Override
    @Transactional(readOnly = true)
    public Cliente buscarPorCorreo(String correo) {
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("El correo de búsqueda no puede estar vacío");
        }
        return clienteRepository.buscarPorCorreo(correo.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "correo", correo));
    }

    @Override
    @Transactional(readOnly = true)
    public List<Cliente> buscarPorNombreContiene(String termino) {
        if (termino == null || termino.isBlank()) {
            return listarTodos();
        }
        return clienteRepository.buscarPorNombreContiene(termino.trim());
    }
}
