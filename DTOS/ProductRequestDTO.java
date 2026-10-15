package com.servicio.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductRequestDTO {

    @NotBlank private String name;
    private String description;
    private String imageUrl;

    @NotNull @DecimalMin("0.01")
    private BigDecimal price;

    @DecimalMin("0.0") @DecimalMax("100.0")
    private BigDecimal discountPercent;

    @NotNull @Min(0)
    private Integer stockQuantity;

    @Min(0)
    private Integer minStock;

    @NotNull
    private Long categoryId;
}
