package com.ecocompra.tech.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.NamedQueries;
import jakarta.persistence.NamedQuery;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Entity
@Table(name = "cliente")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "ventas")
@NamedQueries({
    @NamedQuery(
        name = "Cliente.buscarPorCorreo",
        query = "SELECT c FROM Cliente c WHERE LOWER(c.correo) = LOWER(:correo)"
    ),
    @NamedQuery(
        name = "Cliente.buscarPorNombreContiene",
        query = "SELECT c FROM Cliente c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :termino, '%')) ORDER BY c.nombre ASC"
    )
})
public class Cliente implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cliente")
    private Long idCliente;

    @NotBlank(message = "El nombre del cliente es obligatorio")
    @Size(max = 120, message = "El nombre no debe superar 120 caracteres")
    @Column(name = "nombre", nullable = false, length = 120)
    private String nombre;

    @Email(message = "El correo debe tener un formato válido")
    @Size(max = 150, message = "El correo no debe superar 150 caracteres")
    @Column(name = "correo", length = 150, unique = true)
    private String correo;

    @Size(max = 20, message = "El teléfono no debe superar 20 caracteres")
    @Column(name = "telefono", length = 20)
    private String telefono;

    @JsonIgnore
    @OneToMany(mappedBy = "cliente", cascade = CascadeType.ALL, orphanRemoval = false)
    @Builder.Default
    private List<Venta> ventas = new ArrayList<>();
}
