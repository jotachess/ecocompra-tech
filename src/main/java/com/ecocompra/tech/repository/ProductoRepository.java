
package com.ecocompra.tech.repository;

import com.ecocompra.tech.entity.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}
