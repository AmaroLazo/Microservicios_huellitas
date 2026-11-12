package com.servicio.product.controller;

import java.math.BigDecimal;
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
    public ResponseEntity<List<ProductResponseDTO>> getAll() {
        return ResponseEntity.ok(productService.getAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @GetMapping("/available")
    public ResponseEntity<List<ProductResponseDTO>> getAvailable() {
        return ResponseEntity.ok(productService.getAvailable());
    }

    @GetMapping("/category/{categoryName}")
    public ResponseEntity<List<ProductResponseDTO>> getByCategory(@PathVariable String categoryName) {
        return ResponseEntity.ok(productService.getByCategory(categoryName));
    }

    @GetMapping("/search")
    public ResponseEntity<List<ProductResponseDTO>> searchByName(@RequestParam String name) {
        return ResponseEntity.ok(productService.searchByName(name));
    }

    @GetMapping("/filter")
    public ResponseEntity<List<ProductResponseDTO>> search(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String categoryName,
            @RequestParam(required = false) Boolean inStock) {
        return ResponseEntity.ok(productService.search(minPrice, maxPrice, categoryName, inStock));
    }

    @PostMapping
    public ResponseEntity<ProductResponseDTO> create(@Valid @RequestBody ProductRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> update(@PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequestDTO request) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @PostMapping("/{id}/stock")
    public ResponseEntity<ProductResponseDTO> adjustStock(@PathVariable Long id,
            @Valid @RequestBody StockAdjustmentRequestDTO request) {
        return ResponseEntity.ok(productService.adjustStock(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
