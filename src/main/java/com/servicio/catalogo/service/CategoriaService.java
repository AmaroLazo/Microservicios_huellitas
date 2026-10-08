package com.servicio.catalogo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.servicio.catalogo.model.Categoria;
import com.servicio.catalogo.repository.CategoriaRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoriaService {

    private final CategoriaRepository categoriaRepository;

    public List<Categoria> obtenerTodas() {
        return categoriaRepository.findAll();
    }

    public Optional<Categoria> obtenerPorId(Long id) {
        return categoriaRepository.findById(id);
    }

    // MODIFICADO: ahora valida nombre único (HU-15 "sin duplicados").
    public Categoria guardar(Categoria c) {
        if (categoriaRepository.existsByNombreIgnoreCase(c.getNombre())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + c.getNombre());
        }
        return categoriaRepository.save(c);
    }

    // PENDIENTE (Fase 1): debe impedir eliminar si tiene productos asociados
    // (sección 5). Hoy borra directo, igual que antes.
    public void eliminar(Long id) {
        categoriaRepository.deleteById(id);
    }
}