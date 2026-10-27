package com.ecocompra.tech.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaRequestDto {

    @NotNull(message = "El identificador de cliente es obligatorio")
    private Long idCliente;

    @NotEmpty(message = "Una venta confirmada debe contener al menos un detalle de producto")
    @Valid
    private List<DetalleVentaRequestDto> detalles;
}
