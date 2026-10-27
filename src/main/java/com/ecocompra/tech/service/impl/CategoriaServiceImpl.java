package com.ecocompra.tech.service.impl;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.ecocompra.tech.dto.CategoriaDto;
import com.ecocompra.tech.exception.IllegalOperationException;
import com.ecocompra.tech.exception.ResourceNotFoundException;
import com.ecocompra.tech.model.Categoria;
import com.ecocompra.tech.repository.CategoriaRepository;
import com.ecocompra.tech.repository.ProductoRepository;
import com.ecocompra.tech.service.CategoriaService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoriaServiceImpl implements CategoriaService {

    private final CategoriaRepository categoriaRepository;
    private final ProductoRepository productoRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Categoria> listarTodas() {
        return categoriaRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Categoria obtenerPorId(Long id) {
        return categoriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría", "id", id));
    }

    @Override
    public Categoria crear(CategoriaDto dto) {
        if (categoriaRepository.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new IllegalOperationException("Ya existe una categoría registrada con el nombre: " + dto.getNombre());
        }

        Categoria categoria = Categoria.builder()
                .nombre(dto.getNombre().trim())
                .descripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null)
                .build();

        return categoriaRepository.save(categoria);
    }

    @Override
    public Categoria actualizar(Long id, CategoriaDto dto) {
        Categoria categoria = obtenerPorId(id);

        if (!categoria.getNombre().equalsIgnoreCase(dto.getNombre().trim())
                && categoriaRepository.existsByNombreIgnoreCase(dto.getNombre().trim())) {
            throw new IllegalOperationException("Ya existe otra categoría con el nombre: " + dto.getNombre());
        }

        categoria.setNombre(dto.getNombre().trim());
        categoria.setDescripcion(dto.getDescripcion() != null ? dto.getDescripcion().trim() : null);

        return categoriaRepository.save(categoria);
    }

    @Override
    public void eliminar(Long id) {
        Categoria categoria = obtenerPorId(id);
        long productosAsociados = productoRepository.findByCategoriaIdCategoria(id).size();
        if (productosAsociados > 0) {
            throw new IllegalOperationException("No se puede eliminar la categoría porque contiene "
                    + productosAsociados + " productos asociados (Restricción ON DELETE RESTRICT).");
        }
        categoriaRepository.delete(categoria);
    }
}
