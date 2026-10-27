package com.ecocompra.tech.controller;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.ecocompra.tech.dto.CategoriaDto;
import com.ecocompra.tech.dto.ProductoDto;
import com.ecocompra.tech.exception.ResourceNotFoundException;
import com.ecocompra.tech.entity.Categoria;
import com.ecocompra.tech.entity.Producto;
import com.ecocompra.tech.service.CategoriaService;

@SpringBootTest
@Transactional
class ProductoControllerTest {

    @Autowired
    private ProductoController productoController;

    @Autowired
    private CategoriaService categoriaService;

    private Categoria categoriaBase;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));

        categoriaBase = categoriaService.crear(CategoriaDto.builder()
                .nombre("Laptops para Pruebas " + System.currentTimeMillis())
                .descripcion("Categoría de prueba")
                .build());
    }

    @Test
    @DisplayName("CP19: GET /api/productos - Listar productos retorna HTTP 200")
    void testListarTodos() {
        ResponseEntity<List<Producto>> respuesta = productoController.listarTodos();
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
    }

    @Test
    @DisplayName("CP20: POST /api/productos - Crear producto retorna HTTP 201 Created y objeto persistido")
    void testCrearProducto() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(categoriaBase.getIdCategoria())
                .nombre("Laptop Controller Pro")
                .precio(new BigDecimal("3500.00"))
                .stock(10)
                .reparabilidad("Manual técnico libre")
                .consumoKwh(new BigDecimal("0.0400"))
                .condicionesConsumo("8 horas al día")
                .fuenteTecnica("https://fuente.com")
                .build();

        ResponseEntity<Producto> respuesta = productoController.crear(dto);
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Laptop Controller Pro", respuesta.getBody().getNombre());
        assertNotNull(respuesta.getHeaders().getLocation());
    }

    @Test
    @DisplayName("CP21: GET /api/productos/{id} - Obtener por ID existente retorna HTTP 200 y por inexistente lanza 404")
    void testObtenerPorId() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(categoriaBase.getIdCategoria())
                .nombre("Monitor Controller")
                .precio(new BigDecimal("900.00"))
                .stock(5)
                .build();
        ResponseEntity<Producto> nuevo = productoController.crear(dto);
        Long id = nuevo.getBody().getIdProducto();

        ResponseEntity<Producto> encontrado = productoController.obtenerPorId(id);
        assertEquals(HttpStatus.OK, encontrado.getStatusCode());
        assertEquals("Monitor Controller", encontrado.getBody().getNombre());

        assertThrows(ResourceNotFoundException.class, () -> productoController.obtenerPorId(99999L));
    }

    @Test
    @DisplayName("CP22: PUT /api/productos/{id} - Actualizar producto retorna HTTP 200 con cambios reflejados")
    void testActualizarProducto() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(categoriaBase.getIdCategoria())
                .nombre("Nombre Original")
                .precio(new BigDecimal("1200.00"))
                .stock(4)
                .build();
        Producto original = productoController.crear(dto).getBody();

        ProductoDto dtoActualizado = ProductoDto.builder()
                .idCategoria(categoriaBase.getIdCategoria())
                .nombre("Nombre Editado")
                .precio(new BigDecimal("1350.00"))
                .stock(6)
                .build();

        ResponseEntity<Producto> respuesta = productoController.actualizar(original.getIdProducto(), dtoActualizado);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Nombre Editado", respuesta.getBody().getNombre());
        assertEquals(new BigDecimal("1350.00"), respuesta.getBody().getPrecio());
    }

    @Test
    @DisplayName("CP23: DELETE /api/productos/{id} - Eliminar producto retorna HTTP 204 No Content")
    void testEliminarProducto() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(categoriaBase.getIdCategoria())
                .nombre("Producto a Descartar")
                .precio(new BigDecimal("200.00"))
                .stock(2)
                .build();
        Producto producto = productoController.crear(dto).getBody();

        ResponseEntity<Void> respuesta = productoController.eliminar(producto.getIdProducto());
        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());

        assertThrows(ResourceNotFoundException.class, () -> productoController.obtenerPorId(producto.getIdProducto()));
    }

    @Test
    @DisplayName("CP24: Consultas JPQL y Named Queries vía ProductoController")
    void testEndpointsBusqueda() {
        ProductoDto dto = ProductoDto.builder()
                .idCategoria(categoriaBase.getIdCategoria())
                .nombre("Ultrabook EcoPlus")
                .precio(new BigDecimal("2700.00"))
                .stock(8)
                .consumoKwh(new BigDecimal("0.0280"))
                .condicionesConsumo("6h/dia")
                .fuenteTecnica("https://fuente.com")
                .build();
        productoController.crear(dto);

        // JPQL: buscar por nombre
        ResponseEntity<List<Producto>> porNombre = productoController.buscarPorNombre("EcoPlus");
        assertEquals(HttpStatus.OK, porNombre.getStatusCode());
        assertFalse(porNombre.getBody().isEmpty());

        // Named Query: buscar por rango de precio
        ResponseEntity<List<Producto>> porPrecio = productoController.buscarPorRangoPrecio(
                new BigDecimal("2500.00"), new BigDecimal("3000.00"));
        assertEquals(HttpStatus.OK, porPrecio.getStatusCode());
        assertFalse(porPrecio.getBody().isEmpty());

        // Named Query: buscar por categoría y stock
        ResponseEntity<List<Producto>> porCatStock = productoController.buscarPorCategoriaYStock(
                categoriaBase.getIdCategoria(), 5);
        assertEquals(HttpStatus.OK, porCatStock.getStatusCode());
        assertFalse(porCatStock.getBody().isEmpty());
    }
}
