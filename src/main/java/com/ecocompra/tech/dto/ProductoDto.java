package com.ecocompra.tech.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoDto {

    private Long idProducto;

    @NotNull(message = "El identificador de categoría es obligatorio")
    private Long idCategoria;

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Size(max = 120, message = "El nombre no puede exceder los 120 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio debe ser mayor o igual a cero")
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock debe ser mayor o igual a cero")
    private Integer stock;

    @Size(max = 500, message = "La reparabilidad no puede exceder los 500 caracteres")
    private String reparabilidad;

    @DecimalMin(value = "0.0000", inclusive = true, message = "El consumo en kWh no puede ser negativo")
    private BigDecimal consumoKwh;

    @Size(max = 500, message = "Las condiciones de consumo no pueden exceder los 500 caracteres")
    private String condicionesConsumo;

    @DecimalMin(value = "0.01", inclusive = true, message = "La vida útil debe ser mayor a cero")
    private BigDecimal vidaUtilAnios;

    @Size(max = 1000, message = "La fuente técnica no puede exceder los 1000 caracteres")
    private String fuenteTecnica;
}
