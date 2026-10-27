package com.ecocompra.tech;

import com.ecocompra.tech.entity.*;
import com.ecocompra.tech.repository.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PersistenciaApiIntegrationTest {
    @Autowired MockMvc mvc;
    @Autowired CategoriaRepository categorias;
    @Autowired ProductoRepository productos;
    @Autowired ClienteRepository clientes;
    @Autowired VentaRepository ventas;
    @Autowired DetalleVentaRepository detalles;

    private Categoria categoria;

    @BeforeEach
    void preparar() {
        detalles.deleteAll();
        ventas.deleteAll();
        productos.deleteAll();
        categorias.deleteAll();
        clientes.deleteAll();
        categoria = new Categoria();
        categoria.setNombre("Laptops");
        categoria = categorias.saveAndFlush(categoria);
    }

    @Test
    void crudClientesPorHttp() throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ana\",\"correo\":\"ana@example.com\"}"))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.idCliente").isNumber());
        Long id = clientes.findAll().getFirst().getIdCliente();
        mvc.perform(get("/api/clientes")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].nombre").value("Ana"));
        mvc.perform(put("/api/clientes/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ana actualizada\"}"))
                .andExpect(status().isOk());
        mvc.perform(get("/api/clientes/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Ana actualizada"));
        mvc.perform(delete("/api/clientes/{id}", id)).andExpect(status().isNoContent());
        assertFalse(clientes.existsById(id));
    }

    @Test
    void crudProductosSerializaCategoriaSinSesionAbierta() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content(productoJson("\"nombre\":\"Laptop\",\"precio\":1200,\"stock\":3")))
                .andExpect(status().isCreated()).andExpect(jsonPath("$.categoria.nombre").value("Laptops"));
        Long id = productos.findAll().getFirst().getIdProducto();
        mvc.perform(get("/api/productos")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].categoria.nombre").value("Laptops"));
        mvc.perform(put("/api/productos/{id}", id).contentType(MediaType.APPLICATION_JSON)
                .content(productoJson("\"nombre\":\"Laptop nueva\",\"precio\":1100,\"stock\":2")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/productos/{id}", id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.nombre").value("Laptop nueva"))
                .andExpect(jsonPath("$.categoria.nombre").value("Laptops"));
        mvc.perform(delete("/api/productos/{id}", id)).andExpect(status().isNoContent());
        assertFalse(productos.existsById(id));
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "{\"nombre\":\" \"}",
        "{\"nombre\":\"Ana\",\"correo\":\"correo-invalido\"}",
        "{\"nombre\":\"Ana\",\"telefono\":\"123456789012345678901\"}"
    })
    void rechazaClientesInvalidos(String json) throws Exception {
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON).content(json))
                .andExpect(status().isBadRequest());
        assertEquals(0, clientes.count());
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "\"nombre\":\" \",\"precio\":10",
        "\"nombre\":\"Laptop\"",
        "\"nombre\":\"Laptop\",\"precio\":-1",
        "\"nombre\":\"Laptop\",\"precio\":10,\"stock\":-1",
        "\"nombre\":\"Laptop\",\"precio\":10.123",
        "\"nombre\":\"Laptop\",\"precio\":10,\"vidaUtilAnios\":0",
        "\"nombre\":\"Laptop\",\"precio\":10,\"consumoKwh\":1,\"fuenteTecnica\":\"Ficha\"",
        "\"nombre\":\"Laptop\",\"precio\":10,\"reparabilidad\":\"Repuestos disponibles\""
    })
    void rechazaProductosQueIncumplenElEsquema(String campos) throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content(productoJson(campos))).andExpect(status().isBadRequest());
        assertEquals(0, productos.count());
    }

    @Test
    void aceptaDatosSosteniblesConCondicionesYFuente() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content(productoJson("""
                    "nombre":"Laptop","precio":1200,"consumoKwh":0.4,
                    "condicionesConsumo":"Durante ocho horas de uso",
                    "vidaUtilAnios":5,"fuenteTecnica":"Ficha del fabricante"
                    """)))
                .andExpect(status().isCreated());
        assertEquals(1, productos.count());
    }

    @Test
    void crearConIdNoSobrescribeRegistros() throws Exception {
        Cliente cliente = clienteGuardado();
        mvc.perform(post("/api/clientes").contentType(MediaType.APPLICATION_JSON)
                .content("{\"idCliente\":" + cliente.getIdCliente() + ",\"nombre\":\"Otro\"}"))
                .andExpect(status().isBadRequest());
        Producto producto = productoGuardado();
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content(productoJson("\"idProducto\":" + producto.getIdProducto()
                        + ",\"nombre\":\"Otro\",\"precio\":1")))
                .andExpect(status().isBadRequest());
        assertEquals("Ana", clientes.findById(cliente.getIdCliente()).orElseThrow().getNombre());
        assertEquals("Laptop", productos.findById(producto.getIdProducto()).orElseThrow().getNombre());
    }

    @Test
    void exigeCategoriaExistenteSinCrearCategoriasDesdeProducto() throws Exception {
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Laptop\",\"precio\":10,\"categoria\":{}}"))
                .andExpect(status().isBadRequest());
        mvc.perform(post("/api/productos").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Laptop\",\"precio\":10,\"categoria\":{\"idCategoria\":999999}}"))
                .andExpect(status().isNotFound());
        assertEquals(0, productos.count());
        assertEquals(1, categorias.count());
    }

    @Test
    void cincoRepositoriosConservanVentasAnteIntentosDeBorrado() throws Exception {
        Cliente cliente = clienteGuardado();
        Producto producto = productoGuardado();
        Venta venta = new Venta();
        venta.setCliente(cliente);
        venta.setFecha(LocalDateTime.now());
        venta = ventas.saveAndFlush(venta);
        DetalleVenta detalle = new DetalleVenta();
        detalle.setVenta(venta);
        detalle.setProducto(producto);
        detalle.setCantidad(1);
        detalle.setPrecioUnitario(producto.getPrecio());
        detalle = detalles.saveAndFlush(detalle);

        mvc.perform(delete("/api/clientes/{id}", cliente.getIdCliente()))
                .andExpect(status().isConflict());
        mvc.perform(delete("/api/productos/{id}", producto.getIdProducto()))
                .andExpect(status().isConflict());
        assertTrue(clientes.existsById(cliente.getIdCliente()));
        assertTrue(productos.existsById(producto.getIdProducto()));
        assertTrue(ventas.existsById(venta.getIdVenta()));
        assertTrue(detalles.existsById(detalle.getIdDetalle()));
    }

    @Test
    void idsInexistentesDevuelven404() throws Exception {
        for (String recurso : new String[]{"clientes", "productos"}) {
            mvc.perform(get("/api/" + recurso + "/999999")).andExpect(status().isNotFound());
            mvc.perform(delete("/api/" + recurso + "/999999")).andExpect(status().isNotFound());
        }
        mvc.perform(put("/api/clientes/999999").contentType(MediaType.APPLICATION_JSON)
                .content("{\"nombre\":\"Ana\"}")).andExpect(status().isNotFound());
        mvc.perform(put("/api/productos/999999").contentType(MediaType.APPLICATION_JSON)
                .content(productoJson("\"nombre\":\"Laptop\",\"precio\":10")))
                .andExpect(status().isNotFound());
    }

    private String productoJson(String campos) {
        return "{\"categoria\":{\"idCategoria\":" + categoria.getIdCategoria() + "}," + campos + "}";
    }

    private Cliente clienteGuardado() {
        Cliente cliente = new Cliente();
        cliente.setNombre("Ana");
        return clientes.saveAndFlush(cliente);
    }

    private Producto productoGuardado() {
        Producto producto = new Producto();
        producto.setNombre("Laptop");
        producto.setCategoria(categoria);
        producto.setPrecio(new BigDecimal("1200.00"));
        producto.setStock(3);
        return productos.saveAndFlush(producto);
    }
}
