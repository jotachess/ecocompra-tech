package com.ecocompra.tech.entity;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "producto")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = {"categoria", "detalles"})
@NamedQueries({
    @NamedQuery(
        name = "Producto.buscarPorRangoPrecio",
        query = "SELECT p FROM Producto p WHERE p.precio BETWEEN :precioMin AND :precioMax ORDER BY p.precio ASC"
    ),
    @NamedQuery(
        name = "Producto.buscarPorCategoriaYStock",
        query = "SELECT p FROM Producto p WHERE p.categoria.idCategoria = :idCategoria AND p.stock >= :stockMin ORDER BY p.nombre ASC"
    ),
    @NamedQuery(
        name = "Producto.buscarPorReparabilidadContiene",
        query = "SELECT p FROM Producto p WHERE LOWER(p.reparabilidad) LIKE LOWER(CONCAT('%', :criterio, '%')) ORDER BY p.nombre ASC"
    )
})
public class Producto implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_producto")
    private Long idProducto;

    @NotNull(message = "La categoría del producto es obligatoria")
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @NotBlank(message = "El nombre del producto es obligatorio")
    @Size(max = 120, message = "El nombre no debe superar 120 caracteres")
    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Size(max = 500, message = "La descripción no debe superar 500 caracteres")
    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @NotNull(message = "El precio es obligatorio")
    @DecimalMin(value = "0.00", inclusive = true, message = "El precio no puede ser negativo")
    @Column(name = "precio", nullable = false, precision = 10, scale = 2)
    @Digits(integer = 8, fraction = 2)
    private BigDecimal precio;

    @NotNull(message = "El stock es obligatorio")
    @Min(value = 0, message = "El stock no puede ser negativo")
    @Column(name = "stock", nullable = false)
    private Integer stock;

    @Size(max = 500, message = "La información de reparabilidad no debe superar 500 caracteres")
    @Column(name = "reparabilidad", length = 500)
    private String reparabilidad;

    @DecimalMin(value = "0.0000", inclusive = true, message = "El consumo en kWh no puede ser negativo")
    @Column(name = "consumo_kwh", precision = 12, scale = 4)
    @Digits(integer = 8, fraction = 4)
    private BigDecimal consumoKwh;

    @Size(max = 500, message = "Las condiciones de consumo no deben superar 500 caracteres")
    @Column(name = "condiciones_consumo", length = 500)
    private String condicionesConsumo;

    @DecimalMin(value = "0.01", inclusive = true, message = "La vida útil estimada debe ser mayor a cero")
    @Column(name = "vida_util_anios", precision = 5, scale = 2)
    @Digits(integer = 3, fraction = 2)
    private BigDecimal vidaUtilAnios;

    @Size(max = 1000, message = "La fuente técnica no debe superar 1000 caracteres")
    @Column(name = "fuente_tecnica", length = 1000)
    private String fuenteTecnica;

    @JsonIgnore
    @OneToMany(mappedBy = "producto")
    @Builder.Default
    private List<DetalleVenta> detalles = new ArrayList<>();
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
