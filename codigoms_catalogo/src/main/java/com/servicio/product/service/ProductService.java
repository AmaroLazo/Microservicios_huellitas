package com.servicio.product.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicio.product.dto.ProductRequestDTO;
import com.servicio.product.dto.ProductResponseDTO;
import com.servicio.product.dto.ProductUpdateRequestDTO;
import com.servicio.product.dto.StockAdjustmentRequestDTO;
import com.servicio.product.model.Category;
import com.servicio.product.model.Product;
import com.servicio.product.model.StockMovement;
import com.servicio.product.repository.CategoryRepository;
import com.servicio.product.repository.ProductRepository;
import com.servicio.product.repository.StockMovementRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;
    private final StockMovementRepository stockMovementRepository;

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAll() {
        return productRepository.findAll().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public ProductResponseDTO getById(Long id) {
        return toDTO(findProduct(id));
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getAvailable() {
        return productRepository.findByActiveTrue().stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getByCategory(String categoryName) {
        return productRepository.findByCategory_NameIgnoreCaseAndActiveTrue(categoryName)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new RuntimeException("Categoría no encontrada con id: " + request.getCategoryId()));

        Product product = Product.createProduct(
                request.getName(), request.getDescription(), request.getPrice(), request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setDiscountPercent(
                request.getDiscountPercent() != null ? request.getDiscountPercent() : BigDecimal.ZERO);
        product.setMinStock(request.getMinStock() != null ? request.getMinStock() : 0);
        product.assignCategory(category);

        return toDTO(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductUpdateRequestDTO request) {
        Product product = findProduct(id);
        product.updateProduct(request.getName(), request.getDescription(), request.getImageUrl(), request.getPrice());
        return toDTO(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDTO adjustStock(Long id, StockAdjustmentRequestDTO request) {
        Product product = findProduct(id);
        StockMovement movement = product.adjustInventory(request.getQuantity(), request.getReason());
        productRepository.save(product);
        stockMovementRepository.save(movement);
        return toDTO(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findProduct(id);
        product.deleteProduct();
        productRepository.save(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Producto no encontrado con id: " + id));
    }

    private ProductResponseDTO toDTO(Product p) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setImageUrl(p.getImageUrl() != null ? p.getImageUrl() : "/images/producto-default.png");
        dto.setPrice(p.getPrice());

        BigDecimal discount = p.getDiscountPercent() != null ? p.getDiscountPercent() : BigDecimal.ZERO;
        dto.setDiscountPercent(discount);
        dto.setOnSale(discount.compareTo(BigDecimal.ZERO) > 0);
        dto.setFinalPrice(p.getFinalPrice());

        dto.setCategoryId(p.getCategory().getId());
        dto.setCategoryName(p.getCategory().getName());
        dto.setStockQuantity(p.getStockQuantity());
        dto.setMinStock(p.getMinStock());
        dto.setLowStock(p.isLowStock());
        dto.setActive(Boolean.TRUE.equals(p.getActive()));
        dto.setAvailable(Boolean.TRUE.equals(p.getActive()) && p.hasStock(1));
        return dto;
    }
}
