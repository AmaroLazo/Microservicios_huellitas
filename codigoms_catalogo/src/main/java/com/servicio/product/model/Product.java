package com.servicio.product.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    private String imageUrl;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer minStock = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    // ---- Comportamiento del diagrama ----

    public static Product createProduct(String name, String description, BigDecimal price, Integer stock) {
        if (stock == null || stock < 0) {
            throw new RuntimeException("El stock no puede ser negativo");
        }
        Product product = new Product();
        product.setName(name);
        product.setDescription(description);
        product.updatePrice(price);
        product.setStockQuantity(stock);
        product.setActive(true);
        return product;
    }

    public void updateProduct(String name, String description, String imageUrl, BigDecimal price) {
        this.name = name;
        this.description = description;
        this.imageUrl = imageUrl;
        updatePrice(price);
    }

    // Eliminación lógica: el producto queda inactivo y se conserva su historial de movimientos.
    public void deleteProduct() {
        this.active = false;
    }

    public void updatePrice(BigDecimal newPrice) {
        if (newPrice == null || newPrice.compareTo(new BigDecimal("0.01")) < 0) {
            throw new RuntimeException("El precio debe ser mayor o igual a 0.01");
        }
        this.price = newPrice;
    }

    // finalPrice = price - (price * discountPercent / 100), redondeado a 2 decimales
    public BigDecimal getFinalPrice() {
        BigDecimal discount = discountPercent != null ? discountPercent : BigDecimal.ZERO;
        BigDecimal factor = BigDecimal.valueOf(100).subtract(discount);
        return price.multiply(factor).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    public Boolean hasStock(Integer quantity) {
        return stockQuantity != null && quantity != null && stockQuantity >= quantity;
    }

    public Boolean isLowStock() {
        return stockQuantity != null && minStock != null && stockQuantity <= minStock;
    }

    // quantity positivo = entrada, negativo = salida. Devuelve el movimiento a registrar.
    public StockMovement adjustInventory(Integer quantity, String reason) {
        if (quantity == null || quantity == 0) {
            throw new RuntimeException("La cantidad del ajuste no puede ser cero");
        }
        int newStock = stockQuantity + quantity;
        if (newStock < 0) {
            throw new RuntimeException("Stock insuficiente para el producto: " + name);
        }
        this.stockQuantity = newStock;
        return new StockMovement(null, id, quantity, reason, LocalDateTime.now());
    }

    public void assignCategory(Category category) {
        if (category == null) {
            throw new RuntimeException("La categoría es obligatoria");
        }
        if (!Boolean.TRUE.equals(category.getActive())) {
            throw new RuntimeException("No se puede asociar a una categoría inactiva: " + category.getName());
        }
        this.category = category;
    }
}
