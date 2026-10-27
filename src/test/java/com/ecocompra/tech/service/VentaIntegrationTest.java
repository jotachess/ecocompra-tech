package com.ecocompra.tech.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.ecocompra.tech.dto.CategoriaDto;
import com.ecocompra.tech.dto.ClienteDto;
import com.ecocompra.tech.dto.DetalleVentaRequestDto;
import com.ecocompra.tech.dto.ProductoDto;
import com.ecocompra.tech.dto.VentaRequestDto;
import com.ecocompra.tech.exception.IllegalOperationException;
import com.ecocompra.tech.entity.Categoria;
import com.ecocompra.tech.entity.Cliente;
import com.ecocompra.tech.entity.Producto;
import com.ecocompra.tech.entity.Venta;

@SpringBootTest
@Transactional
class VentaIntegrationTest {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Test
    @DisplayName("CP17: Registrar y recuperar venta completa con múltiples productos y verificar claves foráneas")
    void testRegistrarVentaCompleta() {
        // 1. Preparar datos base
        Categoria cat = categoriaService.crear(CategoriaDto.builder()
                .nombre("Tecnología Sostenible " + System.currentTimeMillis())
                .build());

        Producto p1 = productoService.crear(ProductoDto.builder()
                .idCategoria(cat.getIdCategoria())
                .nombre("Laptop Eco 1")
                .precio(new BigDecimal("1000.00"))
                .stock(10)
                .build());

        Producto p2 = productoService.crear(ProductoDto.builder()
                .idCategoria(cat.getIdCategoria())
                .nombre("Monitor Eco 2")
                .precio(new BigDecimal("500.00"))
                .stock(20)
                .build());

        Cliente cliente = clienteService.crear(ClienteDto.builder()
                .nombre("Instituto de Investigacion")
                .correo("investigacion" + System.currentTimeMillis() + "@inst.pe")
                .build());

        // 2. Registrar venta de 2 laptops y 3 monitores
        // Subtotal esperado: (2 * 1000.00) + (3 * 500.00) = 2000 + 1500 = 3500.00
        VentaRequestDto request = VentaRequestDto.builder()
                .idCliente(cliente.getIdCliente())
                .detalles(List.of(
                        DetalleVentaRequestDto.builder()
                                .idProducto(p1.getIdProducto())
                                .cantidad(2)
                                .build(),
                        DetalleVentaRequestDto.builder()
                                .idProducto(p2.getIdProducto())
                                .cantidad(3)
                                .build()
                ))
                .build();

        Venta venta = ventaService.registrarVenta(request);

        // 3. Validar cabecera y relaciones
        assertNotNull(venta.getIdVenta());
        assertEquals(cliente.getIdCliente(), venta.getCliente().getIdCliente());
        assertEquals(2, venta.getDetalles().size());

        // 4. Validar cálculo del total
        assertEquals(new BigDecimal("3500.00"), venta.calcularTotal());

        // 5. Validar descuento de stock
        Producto p1Actualizado = productoService.obtenerPorId(p1.getIdProducto());
        Producto p2Actualizado = productoService.obtenerPorId(p2.getIdProducto());
        assertEquals(8, p1Actualizado.getStock()); // 10 - 2 = 8
        assertEquals(17, p2Actualizado.getStock()); // 20 - 3 = 17

        // 6. Validar recuperación por ID y por cliente
        Venta recuperada = ventaService.obtenerPorId(venta.getIdVenta());
        assertNotNull(recuperada);
        assertFalse(ventaService.listarPorCliente(cliente.getIdCliente()).isEmpty());
    }

    @Test
    @DisplayName("CP18: Registrar venta con stock insuficiente debe ser rechazada")
    void testRegistrarVentaStockInsuficienteFalla() {
        Categoria cat = categoriaService.crear(CategoriaDto.builder()
                .nombre("Stock Limitado " + System.currentTimeMillis())
                .build());

        Producto p = productoService.crear(ProductoDto.builder()
                .idCategoria(cat.getIdCategoria())
                .nombre("Laptop Ultima Unidad")
                .precio(new BigDecimal("1500.00"))
                .stock(1)
                .build());

        Cliente cliente = clienteService.crear(ClienteDto.builder()
                .nombre("Comprador Exigente")
                .correo("exigente" + System.currentTimeMillis() + "@test.pe")
                .build());

        VentaRequestDto request = VentaRequestDto.builder()
                .idCliente(cliente.getIdCliente())
                .detalles(List.of(
                        DetalleVentaRequestDto.builder()
                                .idProducto(p.getIdProducto())
                                .cantidad(5) // Pide 5 habiendo solo 1
                                .build()
                ))
                .build();

        assertThrows(IllegalOperationException.class, () -> ventaService.registrarVenta(request));
    }
}
