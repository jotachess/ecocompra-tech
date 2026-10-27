package com.ecocompra.tech.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Positive;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor(onConstructor_ = @com.fasterxml.jackson.annotation.JsonCreator)
@AllArgsConstructor
public class ProductoDto {

    private Long idProducto;

    private Long idCategoria;

    // Compatibilidad con el cuerpo {"categoria":{"idCategoria":...}} del PR de Diana.
    private CategoriaReferencia categoria;

    public record CategoriaReferencia(Long idCategoria) {}

    @NotNull(message = "El identificador de categoría es obligatorio")
    @Positive
    public Long getIdCategoria() {
        return idCategoria != null ? idCategoria : categoria != null ? categoria.idCategoria() : null;
    }

    @JsonIgnore
    @AssertTrue(message = "Los identificadores de categoría deben coincidir")
    public boolean isCategoriaCompatible() {
        return idCategoria == null || categoria == null
                || java.util.Objects.equals(idCategoria, categoria.idCategoria());
    }

    @NotBlank(message = "El nombre del producto no puede estar vacío")
    @Size(max = 120, message = "El nombre no puede exceder los 120 caracteres")
    private String nombre;

    @Size(max = 500, message = "La descripción no puede exceder los 500 caracteres")
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio debe ser mayor o igual a cero")
    @Digits(integer = 8, fraction = 2)
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock debe ser mayor o igual a cero")
    @Builder.Default
    private Integer stock = 0;

    @Size(max = 500, message = "La reparabilidad no puede exceder los 500 caracteres")
    private String reparabilidad;

    @DecimalMin(value = "0.0000", inclusive = true, message = "El consumo en kWh no puede ser negativo")
    @Digits(integer = 8, fraction = 4)
    private BigDecimal consumoKwh;

    @Size(max = 500, message = "Las condiciones de consumo no pueden exceder los 500 caracteres")
    private String condicionesConsumo;

    @DecimalMin(value = "0.01", inclusive = true, message = "La vida útil debe ser mayor a cero")
    @Digits(integer = 3, fraction = 2)
    private BigDecimal vidaUtilAnios;

    @Size(max = 1000, message = "La fuente técnica no puede exceder los 1000 caracteres")
    private String fuenteTecnica;

    @JsonIgnore
    @AssertTrue(message = "El consumo requiere condiciones de medición")
    public boolean isCondicionesConsumoValidas() {
        return consumoKwh == null
                || (condicionesConsumo != null && !condicionesConsumo.isBlank());
    }

    @JsonIgnore
    @AssertTrue(message = "Los datos de sostenibilidad requieren una fuente técnica")
    public boolean isFuenteTecnicaValida() {
        boolean tieneDatos = consumoKwh != null || vidaUtilAnios != null
                || (reparabilidad != null && !reparabilidad.isBlank());
        return !tieneDatos || (fuenteTecnica != null && !fuenteTecnica.isBlank());
    }
}
