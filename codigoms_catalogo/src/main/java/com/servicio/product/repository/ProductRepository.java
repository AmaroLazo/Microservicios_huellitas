package com.servicio.product.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.product.model.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByActiveTrue();
    List<Product> findByCategory_NameIgnoreCaseAndActiveTrue(String categoryName);
    List<Product> findByNameContainingIgnoreCaseAndActiveTrue(String name);
}
