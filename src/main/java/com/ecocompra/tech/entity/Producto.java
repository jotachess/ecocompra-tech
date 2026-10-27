package com.ecocompra.tech.entity;

import java.math.BigDecimal;

import jakarta.persistence.*;
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

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "id_categoria", nullable = false)
    private Categoria categoria;

    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Column(name = "descripcion", length = 500)
    private String descripcion;

    @Column(name = "precio", nullable = false,
            precision = 10, scale = 2)
    private BigDecimal precio;

    @Column(name = "stock", nullable = false)
    private Integer stock = 0;

    @Column(name = "reparabilidad", length = 500)
    private String reparabilidad;

    @Column(name = "consumo_kwh", precision = 12, scale = 4)
    private BigDecimal consumoKwh;

    @Column(name = "condiciones_consumo", length = 500)
    private String condicionesConsumo;

    @Column(name = "vida_util_anios", precision = 5, scale = 2)
    private BigDecimal vidaUtilAnios;

    @Column(name = "fuente_tecnica", length = 1000)
    private String fuenteTecnica;
}