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
            "actividad", "Actividad 02",
            "baseDeDatos", "Persistencia JPA configurada",
            "criterios", List.of(
                "Reparabilidad",
                "Consumo energetico",
                "Vida util"
            )
        );
    }
}
