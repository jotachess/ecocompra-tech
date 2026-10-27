package com.ecocompra.tech.service;

import java.time.LocalDateTime;
import java.util.List;

import com.ecocompra.tech.dto.VentaRequestDto;
import com.ecocompra.tech.entity.Venta;

public interface VentaService {

    List<Venta> listarTodas();

    Venta obtenerPorId(Long id);

    Venta registrarVenta(VentaRequestDto request);

    List<Venta> listarPorCliente(Long idCliente);

    List<Venta> buscarPorRangoFechas(LocalDateTime desde, LocalDateTime hasta);
}
