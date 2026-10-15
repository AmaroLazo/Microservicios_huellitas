package com.servicio.product.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.product.dto.ProductRequestDTO;
import com.servicio.product.dto.ProductResponseDTO;
import com.servicio.product.dto.ProductUpdateRequestDTO;
import com.servicio.product.dto.StockAdjustmentRequestDTO;
import com.servicio.product.service.ProductService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @GetMapping
    public List<ProductResponseDTO> getAll() {
        return productService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(productService.getById(id));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/available")
    public List<ProductResponseDTO> getAvailable() {
        return productService.getAvailable();
    }

    @GetMapping("/category/{categoryName}")
    public List<ProductResponseDTO> getByCategory(@PathVariable String categoryName) {
        return productService.getByCategory(categoryName);
    }

    @GetMapping("/search")
    public List<ProductResponseDTO> searchByName(@RequestParam String name) {
        return productService.searchByName(name);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponseDTO create(@Valid @RequestBody ProductRequestDTO request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponseDTO update(@PathVariable Long id, @Valid @RequestBody ProductUpdateRequestDTO request) {
        return productService.update(id, request);
    }

    @PostMapping("/{id}/stock")
    public ProductResponseDTO adjustStock(@PathVariable Long id, @Valid @RequestBody StockAdjustmentRequestDTO request) {
        return productService.adjustStock(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        productService.delete(id);
    }
}
