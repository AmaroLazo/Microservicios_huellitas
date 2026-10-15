package com.servicio.product.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.servicio.product.model.Category;
import com.servicio.product.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    public Optional<Category> getById(Long id) {
        return categoryRepository.findById(id);
    }

    // Valida nombre único (HU-15 "sin duplicados").
    public Category save(Category category) {
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new RuntimeException("Ya existe una categoría con el nombre: " + category.getName());
        }
        return categoryRepository.save(category);
    }

    // PENDIENTE (Fase 1): debe impedir eliminar si tiene productos asociados
    // (sección 5). Hoy borra directo, igual que antes.
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }
}
