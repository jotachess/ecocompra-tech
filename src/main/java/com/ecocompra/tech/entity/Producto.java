package com.ecocompra.tech.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
public class Producto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @NotBlank
    @Size(max = 120)
    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Size(max = 500)
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull
    @PositiveOrZero
    @Digits(integer = 8, fraction = 2)
    @Column(name = "precio", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal precio;

    @NotNull
    @PositiveOrZero
    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Size(max = 500)
    @Column(name = "reparabilidad", length = 500)
    private String reparabilidad;

    @PositiveOrZero
    @Digits(integer = 8, fraction = 4)
    @Column(name = "consumo_kwh", precision = 12, scale = 4)
    private BigDecimal consumoKwh;

    @Size(max = 500)
    @Column(name = "condiciones_consumo", length = 500)
    private String condicionesConsumo;

    @Positive
    @Digits(integer = 3, fraction = 2)
    @Column(name = "vida_util_anios", precision = 5, scale = 2)
    private BigDecimal vidaUtilAnios;

    @Size(max = 1000)
    @Column(name = "fuente_tecnica", length = 1000)
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
        return (reparabilidad == null && consumoKwh == null && vidaUtilAnios == null)
                || (fuenteTecnica != null && !fuenteTecnica.isBlank());
    }
}
