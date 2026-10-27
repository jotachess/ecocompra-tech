package com.ecocompra.tech.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
import com.ecocompra.tech.exception.ResourceNotFoundException;
import com.ecocompra.tech.model.Categoria;
import com.ecocompra.tech.model.Cliente;
import com.ecocompra.tech.model.Producto;

@SpringBootTest
@Transactional
class ClienteServiceTest {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private VentaService ventaService;

    // --- 1. PRUEBAS DE REGISTRO (CREATE) ---

    @Test
    @DisplayName("CP10: Registrar cliente válido")
    void testRegistrarClienteValido() {
        ClienteDto dto = ClienteDto.builder()
                .nombre("Empresa EcoSoluciones")
                .correo("contacto" + System.currentTimeMillis() + "@ecosoluciones.pe")
                .telefono("+51999888777")
                .build();

        Cliente creado = clienteService.crear(dto);

        assertNotNull(creado.getIdCliente());
        assertEquals("Empresa EcoSoluciones", creado.getNombre());
        assertEquals("+51999888777", creado.getTelefono());
    }

    @Test
    @DisplayName("CP11: Registrar cliente con correo duplicado debe fallar")
    void testRegistrarClienteCorreoDuplicadoFalla() {
        String correoComun = "duplicado" + System.currentTimeMillis() + "@test.pe";

        clienteService.crear(ClienteDto.builder()
                .nombre("Cliente Uno")
                .correo(correoComun)
                .build());

        ClienteDto duplicado = ClienteDto.builder()
                .nombre("Cliente Dos")
                .correo(correoComun)
                .build();

        assertThrows(IllegalOperationException.class, () -> clienteService.crear(duplicado));
    }

    // --- 2. PRUEBAS DE BÚSQUEDA Y LECTURA (READ) ---

    @Test
    @DisplayName("CP12: Obtener cliente por ID existente e inexistente")
    void testObtenerPorId() {
        Cliente creado = clienteService.crear(ClienteDto.builder()
                .nombre("Cliente Para Buscar")
                .correo("buscar" + System.currentTimeMillis() + "@test.pe")
                .build());

        Cliente recuperado = clienteService.obtenerPorId(creado.getIdCliente());
        assertNotNull(recuperado);
        assertEquals("Cliente Para Buscar", recuperado.getNombre());

        assertThrows(ResourceNotFoundException.class, () -> clienteService.obtenerPorId(99999L));
    }

    // --- 3. PRUEBAS DE ACTUALIZACIÓN (UPDATE) ---

    @Test
    @DisplayName("CP13: Actualizar datos de cliente")
    void testActualizarCliente() {
        Cliente creado = clienteService.crear(ClienteDto.builder()
                .nombre("Nombre Original")
                .correo("original" + System.currentTimeMillis() + "@test.pe")
                .telefono("+51911111111")
                .build());

        ClienteDto dtoActualizado = ClienteDto.builder()
                .nombre("Nombre Actualizado")
                .correo("actualizado" + System.currentTimeMillis() + "@test.pe")
                .telefono("+51922222222")
                .build();

        Cliente actualizado = clienteService.actualizar(creado.getIdCliente(), dtoActualizado);

        assertEquals("Nombre Actualizado", actualizado.getNombre());
        assertEquals("+51922222222", actualizado.getTelefono());
    }

    // --- 4. PRUEBAS DE ELIMINACIÓN Y RESTRICCIÓN REFERENCIAL (DELETE) ---

    @Test
    @DisplayName("CP14: Eliminar cliente sin ventas asociadas debe tener éxito")
    void testEliminarClienteSinVentas() {
        Cliente creado = clienteService.crear(ClienteDto.builder()
                .nombre("Cliente Solitario")
                .correo("solitario" + System.currentTimeMillis() + "@test.pe")
                .build());
        Long id = creado.getIdCliente();

        clienteService.eliminar(id);

        assertThrows(ResourceNotFoundException.class, () -> clienteService.obtenerPorId(id));
    }

    @Test
    @DisplayName("CP15: Eliminar cliente con ventas asociadas debe ser rechazado (ON DELETE RESTRICT)")
    void testEliminarClienteConVentasAsociadasFalla() {
        // 1. Crear cliente
        Cliente cliente = clienteService.crear(ClienteDto.builder()
                .nombre("Cliente Con Compras")
                .correo("concompras" + System.currentTimeMillis() + "@test.pe")
                .build());

        // 2. Crear categoria y producto
        Categoria cat = categoriaService.crear(CategoriaDto.builder()
                .nombre("Accesorios " + System.currentTimeMillis())
                .build());

        Producto prod = productoService.crear(ProductoDto.builder()
                .idCategoria(cat.getIdCategoria())
                .nombre("Mouse Solar")
                .precio(new BigDecimal("80.00"))
                .stock(10)
                .build());

        // 3. Registrar venta
        ventaService.registrarVenta(VentaRequestDto.builder()
                .idCliente(cliente.getIdCliente())
                .detalles(List.of(DetalleVentaRequestDto.builder()
                        .idProducto(prod.getIdProducto())
                        .cantidad(1)
                        .precioUnitario(new BigDecimal("80.00"))
                        .build()))
                .build());

        // 4. Intentar eliminar el cliente
        IllegalOperationException ex = assertThrows(IllegalOperationException.class,
                () -> clienteService.eliminar(cliente.getIdCliente()));
        assertTrue(ex.getMessage().contains("ON DELETE RESTRICT"));
    }

    // --- 5. PRUEBAS DE CONSULTAS JPQL Y NAMED QUERIES ---

    @Test
    @DisplayName("CP16: Consultas JPQL y Named Queries de Cliente")
    void testConsultasCliente() {
        String correo = "verde" + System.currentTimeMillis() + "@consultora.pe";
        clienteService.crear(ClienteDto.builder()
                .nombre("Asociacion Verde Horizonte")
                .correo(correo)
                .telefono("+51977766655")
                .build());

        // JPQL: buscarPorTelefonoJPQL
        List<Cliente> porTelefono = clienteService.buscarPorTelefonoJPQL("+51977766655");
        assertFalse(porTelefono.isEmpty());
        assertEquals("Asociacion Verde Horizonte", porTelefono.get(0).getNombre());

        // Named Query: buscarPorCorreo
        Cliente porCorreo = clienteService.buscarPorCorreo(correo);
        assertNotNull(porCorreo);
        assertEquals("Asociacion Verde Horizonte", porCorreo.getNombre());

        // Named Query: buscarPorNombreContiene
        List<Cliente> porNombre = clienteService.buscarPorNombreContiene("Verde Horizonte");
        assertFalse(porNombre.isEmpty());
    }
}
