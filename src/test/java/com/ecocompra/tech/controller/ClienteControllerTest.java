package com.ecocompra.tech.controller;

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

import com.ecocompra.tech.dto.ClienteDto;
import com.ecocompra.tech.exception.ResourceNotFoundException;
import com.ecocompra.tech.model.Cliente;

@SpringBootTest
@Transactional
class ClienteControllerTest {

    @Autowired
    private ClienteController clienteController;

    @BeforeEach
    void setUp() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        RequestContextHolder.setRequestAttributes(new ServletRequestAttributes(request));
    }

    @Test
    @DisplayName("CP25: GET /api/clientes - Listar clientes retorna HTTP 200")
    void testListarTodos() {
        ResponseEntity<List<Cliente>> respuesta = clienteController.listarTodos();
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
    }

    @Test
    @DisplayName("CP26: POST /api/clientes - Crear cliente retorna HTTP 201 Created")
    void testCrearCliente() {
        String correo = "cliente.ctrl" + System.currentTimeMillis() + "@empresa.pe";
        ClienteDto dto = ClienteDto.builder()
                .nombre("Empresa Verde S.A.C.")
                .correo(correo)
                .telefono("+51944332211")
                .build();

        ResponseEntity<Cliente> respuesta = clienteController.crear(dto);
        assertEquals(HttpStatus.CREATED, respuesta.getStatusCode());
        assertNotNull(respuesta.getBody());
        assertEquals("Empresa Verde S.A.C.", respuesta.getBody().getNombre());
        assertNotNull(respuesta.getHeaders().getLocation());
    }

    @Test
    @DisplayName("CP27: GET /api/clientes/{id} - Obtener por ID existente y 404 para inexistente")
    void testObtenerPorId() {
        ClienteDto dto = ClienteDto.builder()
                .nombre("Cliente Consulta")
                .correo("consulta" + System.currentTimeMillis() + "@test.pe")
                .build();
        Cliente nuevo = clienteController.crear(dto).getBody();

        ResponseEntity<Cliente> encontrado = clienteController.obtenerPorId(nuevo.getIdCliente());
        assertEquals(HttpStatus.OK, encontrado.getStatusCode());
        assertEquals("Cliente Consulta", encontrado.getBody().getNombre());

        assertThrows(ResourceNotFoundException.class, () -> clienteController.obtenerPorId(99999L));
    }

    @Test
    @DisplayName("CP28: PUT /api/clientes/{id} - Actualizar cliente retorna HTTP 200")
    void testActualizarCliente() {
        ClienteDto dto = ClienteDto.builder()
                .nombre("Cliente Inicial")
                .correo("ini" + System.currentTimeMillis() + "@test.pe")
                .telefono("+51900000001")
                .build();
        Cliente creado = clienteController.crear(dto).getBody();

        ClienteDto dtoActualizado = ClienteDto.builder()
                .nombre("Cliente Modificado")
                .correo("mod" + System.currentTimeMillis() + "@test.pe")
                .telefono("+51900000002")
                .build();

        ResponseEntity<Cliente> respuesta = clienteController.actualizar(creado.getIdCliente(), dtoActualizado);
        assertEquals(HttpStatus.OK, respuesta.getStatusCode());
        assertEquals("Cliente Modificado", respuesta.getBody().getNombre());
        assertEquals("+51900000002", respuesta.getBody().getTelefono());
    }

    @Test
    @DisplayName("CP29: DELETE /api/clientes/{id} - Eliminar cliente retorna HTTP 204")
    void testEliminarCliente() {
        ClienteDto dto = ClienteDto.builder()
                .nombre("Cliente Para Eliminar")
                .correo("del" + System.currentTimeMillis() + "@test.pe")
                .build();
        Cliente creado = clienteController.crear(dto).getBody();

        ResponseEntity<Void> respuesta = clienteController.eliminar(creado.getIdCliente());
        assertEquals(HttpStatus.NO_CONTENT, respuesta.getStatusCode());

        assertThrows(ResourceNotFoundException.class, () -> clienteController.obtenerPorId(creado.getIdCliente()));
    }

    @Test
    @DisplayName("CP30: Búsquedas JPQL y Named Queries vía ClienteController")
    void testEndpointsBusqueda() {
        String correo = "busqueda" + System.currentTimeMillis() + "@ong.pe";
        ClienteDto dto = ClienteDto.builder()
                .nombre("ONG Planeta Limpio")
                .correo(correo)
                .telefono("+51911223344")
                .build();
        clienteController.crear(dto);

        // JPQL: buscar por teléfono
        ResponseEntity<List<Cliente>> porTelefono = clienteController.buscarPorTelefono("+51911223344");
        assertEquals(HttpStatus.OK, porTelefono.getStatusCode());
        assertFalse(porTelefono.getBody().isEmpty());

        // Named Query: buscar por correo
        ResponseEntity<Cliente> porCorreo = clienteController.buscarPorCorreo(correo);
        assertEquals(HttpStatus.OK, porCorreo.getStatusCode());
        assertEquals("ONG Planeta Limpio", porCorreo.getBody().getNombre());

        // Named Query: buscar por nombre contiene
        ResponseEntity<List<Cliente>> porNombre = clienteController.buscarPorNombre("Planeta Limpio");
        assertEquals(HttpStatus.OK, porNombre.getStatusCode());
        assertFalse(porNombre.getBody().isEmpty());
    }
}
