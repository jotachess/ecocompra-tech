package com.ecocompra.tech.dto;

import jakarta.validation.constraints.Email;
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
public class ClienteDto {

    private Long idCliente;

    @NotBlank(message = "El nombre del cliente no puede estar vacío")
    @Size(max = 120, message = "El nombre no puede exceder 120 caracteres")
    private String nombre;

    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 150, message = "El correo no puede exceder 150 caracteres")
    private String correo;

    @Size(max = 20, message = "El teléfono no puede exceder 20 caracteres")
    private String telefono;
}
