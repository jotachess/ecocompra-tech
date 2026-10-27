package com.ecocompra.tech.service.impl;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecocompra.tech.dto.DetalleVentaRequestDto;
import com.ecocompra.tech.dto.VentaRequestDto;
import com.ecocompra.tech.exception.IllegalOperationException;
import com.ecocompra.tech.exception.ResourceNotFoundException;
import com.ecocompra.tech.entity.Cliente;
import com.ecocompra.tech.entity.DetalleVenta;
import com.ecocompra.tech.entity.Producto;
import com.ecocompra.tech.entity.Venta;
import com.ecocompra.tech.repository.ClienteRepository;
import com.ecocompra.tech.repository.ProductoRepository;
import com.ecocompra.tech.repository.VentaRepository;
import com.ecocompra.tech.service.VentaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class VentaServiceImpl implements VentaService {

    private final VentaRepository ventaRepository;
    private final ClienteRepository clienteRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarTodas() {
        return ventaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Venta obtenerPorId(Long id) {
        return ventaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Venta", "id", id));
    }

    @Override
    public Venta registrarVenta(VentaRequestDto request) {
        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new ResourceNotFoundException("Cliente", "id", request.getIdCliente()));

        if (request.getDetalles() == null || request.getDetalles().isEmpty()) {
            throw new IllegalOperationException("Una venta confirmada debe contener al menos un detalle de producto.");
        }

        Venta venta = Venta.builder()
                .cliente(cliente)
                .fecha(LocalDateTime.now())
                .detalles(new ArrayList<>())
                .build();

        for (DetalleVentaRequestDto itemDto : request.getDetalles()) {
            Producto producto = productoRepository.findById(itemDto.getIdProducto())
                    .orElseThrow(() -> new ResourceNotFoundException("Producto", "id", itemDto.getIdProducto()));

            if (producto.getStock() < itemDto.getCantidad()) {
                throw new IllegalOperationException(String.format(
                        "Stock insuficiente para el producto '%s'. Disponible: %d, Solicitado: %d",
                        producto.getNombre(), producto.getStock(), itemDto.getCantidad()));
            }

            // Descontar stock
            producto.setStock(producto.getStock() - itemDto.getCantidad());
            productoRepository.save(producto);

            // Determinar precio aplicado (conserva el historico en la linea)
            BigDecimal precioAplicado = itemDto.getPrecioUnitario() != null
                    ? itemDto.getPrecioUnitario()
                    : producto.getPrecio();

            DetalleVenta detalle = DetalleVenta.builder()
                    .venta(venta)
                    .producto(producto)
                    .cantidad(itemDto.getCantidad())
                    .precioUnitario(precioAplicado)
                    .build();

            venta.agregarDetalle(detalle);
        }

        return ventaRepository.save(venta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> listarPorCliente(Long idCliente) {
        if (!clienteRepository.existsById(idCliente)) {
            throw new ResourceNotFoundException("Cliente", "id", idCliente);
        }
        return ventaRepository.findByClienteIdCliente(idCliente);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Venta> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta) {
        if (desde == null || hasta == null) {
            throw new IllegalArgumentException("Las fechas de inicio y fin son obligatorias");
        }
        return ventaRepository.buscarPorRangoFechasJPQL(desde, hasta);
    }
}
