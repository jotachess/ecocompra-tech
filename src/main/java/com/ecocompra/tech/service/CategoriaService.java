package com.ecocompra.tech.service;

import java.util.List;

import com.ecocompra.tech.dto.CategoriaDto;
import com.ecocompra.tech.model.Categoria;

public interface CategoriaService {

    List<Categoria> listarTodas();

    Categoria obtenerPorId(Long id);

    Categoria crear(CategoriaDto dto);

    Categoria actualizar(Long id, CategoriaDto dto);

    void eliminar(Long id);
}
