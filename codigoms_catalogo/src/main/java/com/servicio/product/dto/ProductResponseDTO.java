package com.servicio.product.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ProductResponseDTO {
    private Long id;
    private String name;
    private String description;
    private String imageUrl;
    private BigDecimal price;
    private BigDecimal discountPercent;
    private BigDecimal finalPrice;
    private boolean onSale;
    private Long categoryId;
    private String categoryName;
    private Integer stockQuantity;
    private Integer minStock;
    private boolean lowStock;
    private boolean active;
    private boolean available;
}
