package com.servicio.product.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.product.dto.CategoryRequestDTO;
import com.servicio.product.dto.CategoryResponseDTO;
import com.servicio.product.model.Category;
import com.servicio.product.service.CategoryService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryService categoryService;

    @GetMapping
    public ResponseEntity<List<CategoryResponseDTO>> getAll() {
        return ResponseEntity.ok(categoryService.getAll().stream().map(this::toResponse).toList());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> getById(@PathVariable Long id) {
        var category = categoryService.getById(id);
        if (category.isEmpty()) return ResponseEntity.notFound().build();
        return ResponseEntity.ok(toResponse(category.get()));
    }

    @PostMapping
    public ResponseEntity<CategoryResponseDTO> create(@Valid @RequestBody CategoryRequestDTO request) {
        Category category = new Category();
        category.setName(request.getName());
        category.setDescription(request.getDescription());
        category.setActive(request.getActive() == null ? Boolean.TRUE : request.getActive());
        return ResponseEntity.status(201).body(toResponse(categoryService.save(category)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponseDTO> update(@PathVariable Long id,
            @Valid @RequestBody CategoryRequestDTO request) {
        Category category = categoryService.update(id, request.getName(), request.getDescription(), request.getActive());
        return ResponseEntity.ok(toResponse(category));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (categoryService.getById(id).isEmpty()) return ResponseEntity.notFound().build();
        categoryService.delete(id);
        return ResponseEntity.noContent().build();
    }

    private CategoryResponseDTO toResponse(Category category) {
        CategoryResponseDTO response = new CategoryResponseDTO();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setDescription(category.getDescription());
        response.setActive(category.getActive());
        return response;
    }
}
