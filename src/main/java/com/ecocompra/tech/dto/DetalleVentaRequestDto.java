package com.ecocompra.tech.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetalleVentaRequestDto {

    @NotNull(message = "El identificador de producto es obligatorio")
    private Long idProducto;

    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser mayor que cero")
    private Integer cantidad;

    @DecimalMin(value = "0.00", inclusive = true, message = "El precio unitario no puede ser negativo")
    private BigDecimal precioUnitario; // Opcional en el request: si es nulo, el servicio toma el precio actual del producto
}
