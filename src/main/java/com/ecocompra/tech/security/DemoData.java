package com.ecocompra.tech.security;

import java.math.BigDecimal;
import java.util.List;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import com.ecocompra.tech.dto.*;
import com.ecocompra.tech.service.*;
import lombok.RequiredArgsConstructor;

@Component @Profile("demo") @RequiredArgsConstructor
public class DemoData implements CommandLineRunner {
    private final CategoriaService categorias;
    private final ProductoService productos;
    private final ClienteService clientes;
    private final VentaService ventas;
    @Override public void run(String... args) {
        if(!categorias.listarTodas().isEmpty()) return;
        var cat=categorias.crear(CategoriaDto.builder().nombre("Equipos de demostración")
            .descripcion("Datos ficticios para verificar las interfaces").build());
        var laptop=productos.crear(ProductoDto.builder().idCategoria(cat.getIdCategoria())
            .nombre("Laptop de demostración").descripcion("Equipo de ejemplo para el catálogo inicial")
            .precio(new BigDecimal("1890.00")).stock(12).build());
        productos.crear(ProductoDto.builder().idCategoria(cat.getIdCategoria()).nombre("Monitor de demostración")
            .descripcion("Pantalla de ejemplo").precio(new BigDecimal("640.00")).stock(8).build());
        productos.crear(ProductoDto.builder().idCategoria(cat.getIdCategoria()).nombre("Teclado de demostración")
            .descripcion("Accesorio de ejemplo").precio(new BigDecimal("120.00")).stock(15).build());
        var cliente=clientes.crear(ClienteDto.builder().nombre("Laboratorio de ejemplo")
            .correo("laboratorio@example.com").telefono("999000001").build());
        clientes.crear(ClienteDto.builder().nombre("Cliente de demostración").correo("cliente@example.com").build());
        ventas.registrarVenta(VentaRequestDto.builder().idCliente(cliente.getIdCliente())
            .detalles(List.of(DetalleVentaRequestDto.builder().idProducto(laptop.getIdProducto()).cantidad(1).build())).build());
    }
}
