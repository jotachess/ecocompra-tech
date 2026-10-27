package com.ecocompra.tech.controller;

import java.net.URI;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.ecocompra.tech.dto.VentaRequestDto;
import com.ecocompra.tech.model.Venta;
import com.ecocompra.tech.service.VentaService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ventas")
@RequiredArgsConstructor
public class VentaController {

    private final VentaService ventaService;

    @GetMapping
    public ResponseEntity<List<Venta>> listarTodas() {
        return ResponseEntity.ok(ventaService.listarTodas());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Venta> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(ventaService.obtenerPorId(id));
    }

    @PostMapping
    public ResponseEntity<Venta> registrarVenta(@Valid @RequestBody VentaRequestDto request) {
        Venta nueva = ventaService.registrarVenta(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(nueva.getIdVenta())
                .toUri();
        return ResponseEntity.created(ubicacion).body(nueva);
    }

    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Venta>> listarPorCliente(@PathVariable Long idCliente) {
        return ResponseEntity.ok(ventaService.listarPorCliente(idCliente));
    }
}
