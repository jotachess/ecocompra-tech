package com.ecocompra.tech.controller;

import java.util.List;
import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InicioController {

    @GetMapping("/api/estado")
    public Map<String, Object> obtenerEstado() {
        return Map.of(
            "proyecto", "EcoCompra Tech",
            "estado", "EN EJECUCION",
            "actividad", "Actividad 02 - Planificación, Persistencia y Operaciones CRUD",
            "tecnologias", List.of("Java 21", "Spring Boot 4.1.2", "Spring Data JPA", "Hibernate", "MySQL 8"),
            "entidades", List.of("Categoria", "Producto", "Cliente", "Venta", "DetalleVenta"),
            "modulosCRUD", List.of("/api/productos", "/api/clientes", "/api/categorias", "/api/ventas"),
            "consultasParametrizadas", List.of(
                "JPQL: /api/productos/buscar/nombre",
                "JPQL: /api/productos/buscar/consumo",
                "NamedQuery: /api/productos/buscar/precio",
                "NamedQuery: /api/productos/buscar/categoria-stock",
                "NamedQuery: /api/clientes/buscar/correo",
                "NamedQuery: /api/clientes/buscar/nombre"
            )
        );
    }
}