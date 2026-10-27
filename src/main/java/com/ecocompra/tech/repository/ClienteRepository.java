
package com.ecocompra.tech.repository;

import com.ecocompra.tech.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ClienteRepository extends JpaRepository<Cliente, Long> {
}
