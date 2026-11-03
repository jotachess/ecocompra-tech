package com.ecocompra.tech.security;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import org.hibernate.annotations.JdbcTypeCode;
import java.sql.Types;

@Entity
@Table(name = "usuario")
@Getter @Setter
public class Usuario {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true, length = 80)
    private String username;
    @JsonIgnore
    @Column(nullable = false, length = 100)
    private String passwordHash;
    @Enumerated(EnumType.STRING)
    @JdbcTypeCode(Types.VARCHAR) // Coincide con VARCHAR(20) de la migracion MySQL.
    @Column(nullable = false, length = 20)
    private Rol rol;
    @Column(nullable = false)
    private boolean activo = true;
}
