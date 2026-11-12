package com.servicio.product.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
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
        return productRepository.findByActiveTrueAndStockQuantityGreaterThan(0).stream()
                .filter(product -> Boolean.TRUE.equals(product.getCategory().getActive()))
                .map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> getByCategory(String categoryName) {
        return productRepository.findByCategory_NameIgnoreCaseAndActiveTrue(categoryName)
                .stream().filter(product -> Boolean.TRUE.equals(product.getCategory().getActive()))
                .map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> searchByName(String name) {
        return productRepository.findByNameContainingIgnoreCaseAndActiveTrue(name)
                .stream().map(this::toDTO).toList();
    }

    @Transactional(readOnly = true)
    public List<ProductResponseDTO> search(BigDecimal minPrice, BigDecimal maxPrice,
                                           String categoryName, Boolean inStock) {
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("El precio mínimo no puede ser mayor que el máximo");
        }
        return productRepository.searchActiveProducts(minPrice, maxPrice,
                categoryName == null || categoryName.isBlank() ? null : categoryName.trim(), inStock)
                .stream().map(this::toDTO).toList();
    }

    @Transactional
    public ProductResponseDTO create(ProductRequestDTO request) {
        Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));
        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new IllegalArgumentException("No se puede asociar un producto a una categoría inactiva");
        }
        validateDiscount(request.getDiscountPercent());

        Product product = new Product();
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setPrice(request.getPrice());
        product.setDiscountPercent(request.getDiscountPercent() == null ? BigDecimal.ZERO : request.getDiscountPercent());
        product.setStockQuantity(request.getStockQuantity());
        product.setMinStock(request.getMinStock() == null ? 0 : request.getMinStock());
        product.setActive(true);
        product.setCategory(category);
        return toDTO(productRepository.save(product));
    }

    @Transactional
    public ProductResponseDTO update(Long id, ProductUpdateRequestDTO request) {
        Product product = findProductForUpdate(id);
        product.setName(request.getName().trim());
        product.setDescription(request.getDescription());
        product.setImageUrl(request.getImageUrl());
        product.setPrice(request.getPrice());
        validateDiscount(request.getDiscountPercent());
        if (request.getDiscountPercent() != null) {
            product.setDiscountPercent(request.getDiscountPercent());
        }
        if (request.getMinStock() != null) {
            product.setMinStock(request.getMinStock());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new EntityNotFoundException("Categoría no encontrada con id: " + request.getCategoryId()));
            if (!Boolean.TRUE.equals(category.getActive())) {
                throw new IllegalArgumentException("No se puede asignar una categoría inactiva");
            }
            product.setCategory(category);
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getStockQuantity() != null) {
            int anterior = product.getStockQuantity();
            int nuevo = request.getStockQuantity();
            int diferencia = nuevo - anterior;
            if (diferencia != 0) {
                product.setStockQuantity(nuevo);
                stockMovementRepository.save(new StockMovement(null, product.getId(), diferencia,
                        "Actualización desde administración", java.time.LocalDateTime.now()));
            }
        }
        return toDTO(productRepository.save(product));
    }

    /** Actualiza el inventario y registra el movimiento dentro de una sola transacción. */
    @Transactional
    public ProductResponseDTO adjustStock(Long id, StockAdjustmentRequestDTO request) {
        Product product = findProductForUpdate(id);
        Integer quantity = request.getQuantity();
        if (quantity == null || quantity == 0) {
            throw new IllegalArgumentException("La cantidad del ajuste no puede ser cero");
        }
        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalArgumentException("No se puede modificar el stock de un producto inactivo");
        }
        int newStock = product.getStockQuantity() + quantity;
        if (newStock < 0) {
            throw new IllegalArgumentException("Stock insuficiente para el producto: " + product.getName());
        }
        product.setStockQuantity(newStock);
        productRepository.save(product);
        StockMovement movement = new StockMovement(null, product.getId(), quantity,
                request.getReason().trim(), java.time.LocalDateTime.now());
        stockMovementRepository.save(movement);
        return toDTO(product);
    }

    @Transactional
    public void delete(Long id) {
        Product product = findProductForUpdate(id);
        product.setActive(false);
        productRepository.save(product);
    }

    private Product findProduct(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));
    }

    private Product findProductForUpdate(Long id) {
        return productRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new EntityNotFoundException("Producto no encontrado con id: " + id));
    }

    private void validateDiscount(BigDecimal discount) {
        if (discount != null && (discount.compareTo(BigDecimal.ZERO) < 0
                || discount.compareTo(BigDecimal.valueOf(100)) > 0)) {
            throw new IllegalArgumentException("El descuento debe estar entre 0 y 100");
        }
    }

    private BigDecimal finalPrice(Product product) {
        BigDecimal discount = product.getDiscountPercent() == null ? BigDecimal.ZERO : product.getDiscountPercent();
        return product.getPrice().multiply(BigDecimal.valueOf(100).subtract(discount))
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private ProductResponseDTO toDTO(Product p) {
        ProductResponseDTO dto = new ProductResponseDTO();
        dto.setId(p.getId());
        dto.setName(p.getName());
        dto.setDescription(p.getDescription());
        dto.setImageUrl(p.getImageUrl() != null ? p.getImageUrl() : "/images/producto-default.png");
        dto.setPrice(p.getPrice());
        BigDecimal discount = p.getDiscountPercent() == null ? BigDecimal.ZERO : p.getDiscountPercent();
        dto.setDiscountPercent(discount);
        dto.setOnSale(discount.compareTo(BigDecimal.ZERO) > 0);
        dto.setFinalPrice(finalPrice(p));
        dto.setCategoryId(p.getCategory().getId());
        dto.setCategoryName(p.getCategory().getName());
        dto.setStockQuantity(p.getStockQuantity());
        dto.setMinStock(p.getMinStock());
        dto.setLowStock(p.getStockQuantity() <= p.getMinStock());
        dto.setActive(Boolean.TRUE.equals(p.getActive()));
        dto.setAvailable(Boolean.TRUE.equals(p.getActive()) && p.getStockQuantity() > 0);
        return dto;
    }
}
