package com.ecocompra.tech.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaDto {

    private Long idCategoria;

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 120, message = "El nombre de la categoría no puede exceder 120 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;
}
