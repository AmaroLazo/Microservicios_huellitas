package com.servicio.product.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.product.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    boolean existsByNameIgnoreCase(String name);
}
