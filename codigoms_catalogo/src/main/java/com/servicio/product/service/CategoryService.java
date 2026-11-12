package com.servicio.product.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicio.product.model.Category;
import com.servicio.product.repository.CategoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    public List<Category> getAll() {
        return categoryRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Category> getById(Long id) {
        return categoryRepository.findById(id);
    }

    @Transactional
    public Category save(Category category) {
        String name = category.getName() == null ? "" : category.getName().trim();
        if (name.isBlank()) throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        if (categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + name);
        }
        category.setName(name);
        if (category.getActive() == null) category.setActive(true);
        return categoryRepository.save(category);
    }

    @Transactional
    public Category update(Long id, String newName, String newDescription, Boolean newActive) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new jakarta.persistence.EntityNotFoundException("Categoría no encontrada con id: " + id));
        String name = newName == null ? "" : newName.trim();
        if (name.isBlank()) throw new IllegalArgumentException("El nombre de la categoría es obligatorio");
        if (!category.getName().equalsIgnoreCase(name) && categoryRepository.existsByNameIgnoreCase(name)) {
            throw new IllegalArgumentException("Ya existe una categoría con el nombre: " + name);
        }
        category.setName(name);
        category.setDescription(newDescription);
        if (newActive != null) category.setActive(newActive);
        return categoryRepository.save(category);
    }

    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new jakarta.persistence.EntityNotFoundException("Categoría no encontrada con id: " + id);
        }
        categoryRepository.deleteById(id);
    }
}
