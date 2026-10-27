package com.ecocompra.tech.service;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import com.ecocompra.tech.dto.CategoriaDto;
import com.ecocompra.tech.dto.DetalleVentaRequestDto;
import com.ecocompra.tech.dto.ProductoDto;
import com.ecocompra.tech.dto.VentaRequestDto;
import com.ecocompra.tech.exception.IllegalOperationException;
import com.ecocompra.tech.exception.ResourceNotFoundException;
import com.ecocompra.tech.model.Categoria;
import com.ecocompra.tech.model.Cliente;
import com.ecocompra.tech.model.Producto;
import com.ecocompra.tech.repository.ClienteRepository;

@SpringBootTest
@Transactional
class ProductoServiceTest {

    @Autowired
    private ProductoService productoService;

    @Autowired
    private CategoriaService categoriaService;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private VentaService ventaService;

    private Categoria categoriaLaptops;
    private Categoria categoriaMonitores;

    @BeforeEach
    void setUp() {
        categoriaLaptops = categoriaService.crear(CategoriaDto.builder()
                .nombre("Laptops Eco " + System.currentTimeMillis())
                .descripcion("Portátiles sostenibles")
                .build());

        categoriaMonitores = categoriaService.crear(CategoriaDto.builder()
                .nombre("Monitores Eco " + System.currentTimeMillis())
                .descripcion("Monitores de bajo consumo")
                .build());
    }

    // --- 1. PRUEBAS DE REGISTRO (CREATE) ---

    @Test
    @DisplayName("CP01: Registrar producto válido con métricas sostenibles completas")
    void testRegistrarProductoValido() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(categoriaLaptops.getIdCategoria())
                .nombre("Laptop EcoBook Pro 15")
                .descripcion("Portátil de bajo consumo con marco reciclado")
                .precio(new BigDecimal("3200.00"))
                .stock(10)
                .reparabilidad("Facilidad iFixit 8/10")
                .consumoKwh(new BigDecimal("0.0450"))
                .condicionesConsumo("8 horas al día al 50% de brillo")
                .vidaUtilAnios(new BigDecimal("6.00"))
                .fuenteTecnica("https://ecocompra.tech/fichas/ecobook15.pdf")
                .build();

        Producto creado = productoService.crear(dto);

        assertNotNull(creado.getIdProducto());
        assertEquals("Laptop EcoBook Pro 15", creado.getNombre());
        assertEquals(new BigDecimal("3200.00"), creado.getPrecio());
        assertEquals(10, creado.getStock());
        assertEquals(categoriaLaptops.getIdCategoria(), creado.getCategoria().getIdCategoria());
    }

    @Test
    @DisplayName("CP02: Registrar producto con consumo kWh pero sin condiciones debe fallar")
    void testRegistrarProductoConsumoSinCondicionesFalla() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(categoriaLaptops.getIdCategoria())
                .nombre("Laptop Invalida")
                .precio(new BigDecimal("1500.00"))
                .stock(5)
                .consumoKwh(new BigDecimal("0.0500"))
                .condicionesConsumo(null) // Falta condición obligatoria
                .fuenteTecnica("https://fuente.com")
                .build();

        assertThrows(IllegalOperationException.class, () -> productoService.crear(dto));
    }

    @Test
    @DisplayName("CP03: Registrar producto con categoría inexistente debe lanzar ResourceNotFoundException")
    void testRegistrarProductoCategoriaInexistenteFalla() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(99999L)
                .nombre("Producto Sin Categoria")
                .precio(new BigDecimal("100.00"))
                .stock(2)
                .build();

        assertThrows(ResourceNotFoundException.class, () -> productoService.crear(dto));
    }

    // --- 2. PRUEBAS DE BÚSQUEDA Y LECTURA (READ) ---

    @Test
    @DisplayName("CP04: Obtener producto por ID existente y por ID inexistente")
    void testObtenerPorId() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(categoriaMonitores.getIdCategoria())
                .nombre("Monitor Solar 24")
                .precio(new BigDecimal("850.00"))
                .stock(15)
                .build();
        Producto guardado = productoService.crear(dto);

        Producto recuperado = productoService.obtenerPorId(guardado.getIdProducto());
        assertNotNull(recuperado);
        assertEquals("Monitor Solar 24", recuperado.getNombre());

        assertThrows(ResourceNotFoundException.class, () -> productoService.obtenerPorId(88888L));
    }

    // --- 3. PRUEBAS DE ACTUALIZACIÓN (UPDATE) ---

    @Test
    @DisplayName("CP05: Actualizar datos de un producto existente")
    void testActualizarProducto() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(categoriaLaptops.getIdCategoria())
                .nombre("Laptop Original")
                .precio(new BigDecimal("2000.00"))
                .stock(8)
                .build();
        Producto creado = productoService.crear(dto);

        ProductoDto dtoActualizado = ProductoDto.builder()
                .idCategoria(categoriaLaptops.getIdCategoria())
                .nombre("Laptop Modificada")
                .precio(new BigDecimal("2250.00"))
                .stock(12)
                .build();

        Producto actualizado = productoService.actualizar(creado.getIdProducto(), dtoActualizado);

        assertEquals("Laptop Modificada", actualizado.getNombre());
        assertEquals(new BigDecimal("2250.00"), actualizado.getPrecio());
        assertEquals(12, actualizado.getStock());
    }

    // --- 4. PRUEBAS DE ELIMINACIÓN Y RESTRICCIÓN REFERENCIAL (DELETE) ---

    @Test
    @DisplayName("CP06: Eliminar producto sin ventas asociadas debe tener éxito")
    void testEliminarProductoSinVentas() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(categoriaMonitores.getIdCategoria())
                .nombre("Monitor Para Borrar")
                .precio(new BigDecimal("500.00"))
                .stock(3)
                .build();
        Producto creado = productoService.crear(dto);
        Long id = creado.getIdProducto();

        productoService.eliminar(id);

        assertThrows(ResourceNotFoundException.class, () -> productoService.obtenerPorId(id));
    }

    @Test
    @DisplayName("CP07: Eliminar producto con ventas asociadas debe ser rechazado (ON DELETE RESTRICT)")
    void testEliminarProductoConVentasAsociadasFalla() {
        // 1. Crear producto
        ProductoDto prodDto = ProductoDto.builder()
                .idCategoria(categoriaLaptops.getIdCategoria())
                .nombre("Laptop Vendida")
                .precio(new BigDecimal("3000.00"))
                .stock(20)
                .build();
        Producto prod = productoService.crear(prodDto);

        // 2. Crear cliente
        Cliente cliente = clienteRepository.save(Cliente.builder()
                .nombre("Cliente Comprador")
                .correo("comprador" + System.currentTimeMillis() + "@test.pe")
                .build());

        // 3. Registrar venta con este producto
        ventaService.registrarVenta(VentaRequestDto.builder()
                .idCliente(cliente.getIdCliente())
                .detalles(List.of(DetalleVentaRequestDto.builder()
                        .idProducto(prod.getIdProducto())
                        .cantidad(2)
                        .precioUnitario(new BigDecimal("3000.00"))
                        .build()))
                .build());

        // 4. Intentar eliminar el producto debe lanzar IllegalOperationException
        IllegalOperationException ex = assertThrows(IllegalOperationException.class,
                () -> productoService.eliminar(prod.getIdProducto()));
        assertTrue(ex.getMessage().contains("ON DELETE RESTRICT"));
    }

    // --- 5. PRUEBAS DE CONSULTAS JPQL PARAMETRIZADAS ---

    @Test
    @DisplayName("CP08: Consultas JPQL - buscarPorNombreJPQL y buscarPorConsumoMaximoJPQL")
    void testConsultasJPQL() {
        productoService.crear(ProductoDto.builder()
                .idCategoria(categoriaLaptops.getIdCategoria())
                .nombre("Ultrabook EcoSwift")
                .precio(new BigDecimal("2900.00"))
                .stock(5)
                .consumoKwh(new BigDecimal("0.0250"))
                .condicionesConsumo("6h al día")
                .fuenteTecnica("https://fuente.com")
                .build());

        productoService.crear(ProductoDto.builder()
                .idCategoria(categoriaLaptops.getIdCategoria())
                .nombre("Laptop EcoPower")
                .precio(new BigDecimal("4200.00"))
                .stock(4)
                .consumoKwh(new BigDecimal("0.0750"))
                .condicionesConsumo("10h al día")
                .fuenteTecnica("https://fuente.com")
                .build());

        // JPQL: Buscar por nombre coincidente
        List<Producto> encontrados = productoService.buscarPorNombreJPQL("ecoswift");
        assertFalse(encontrados.isEmpty());
        assertTrue(encontrados.stream().anyMatch(p -> p.getNombre().contains("EcoSwift")));

        // JPQL: Buscar por consumo maximo (0.0300) -> Solo EcoSwift debe entrar
        List<Producto> eficientes = productoService.buscarPorConsumoMaximoJPQL(new BigDecimal("0.0300"));
        assertFalse(eficientes.isEmpty());
        assertTrue(eficientes.stream().allMatch(p -> p.getConsumoKwh().compareTo(new BigDecimal("0.0300")) <= 0));
    }

    // --- 6. PRUEBAS DE NAMED QUERIES PARAMETRIZADAS ---

    @Test
    @DisplayName("CP09: Named Queries - buscarPorRangoPrecio y buscarPorCategoriaYStock")
    void testNamedQueries() {
        productoService.crear(ProductoDto.builder()
                .idCategoria(categoriaLaptops.getIdCategoria())
                .nombre("Laptop Gama Baja")
                .precio(new BigDecimal("1200.00"))
                .stock(10)
                .build());

        productoService.crear(ProductoDto.builder()
                .idCategoria(categoriaLaptops.getIdCategoria())
                .nombre("Laptop Gama Media")
                .precio(new BigDecimal("2500.00"))
                .stock(5)
                .build());

        productoService.crear(ProductoDto.builder()
                .idCategoria(categoriaLaptops.getIdCategoria())
                .nombre("Laptop Gama Alta")
                .precio(new BigDecimal("5000.00"))
                .stock(2)
                .build());

        // Named Query: buscarPorRangoPrecio entre 2000 y 3000 -> debe encontrar Gama Media
        List<Producto> rango = productoService.buscarPorRangoPrecio(new BigDecimal("2000.00"), new BigDecimal("3000.00"));
        assertFalse(rango.isEmpty());
        assertTrue(rango.stream().anyMatch(p -> p.getNombre().equals("Laptop Gama Media")));

        // Named Query: buscarPorCategoriaYStock con stock >= 6 -> debe encontrar Gama Baja
        List<Producto> conStock = productoService.buscarPorCategoriaYStock(categoriaLaptops.getIdCategoria(), 6);
        assertFalse(conStock.isEmpty());
        assertTrue(conStock.stream().allMatch(p -> p.getStock() >= 6));
    }
}
