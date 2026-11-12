package com.servicio.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ProductUpdateRequestDTO {

    @NotBlank(message = "El nombre es obligatorio") @Size(max = 150, message = "El nombre admite hasta 150 caracteres") private String name;
    @Size(max = 500, message = "La descripción admite hasta 500 caracteres")
    private String description;
    private String imageUrl;

    @NotNull @DecimalMin("0.01")
    private BigDecimal price;

    @DecimalMin("0.0") @DecimalMax("100.0")
    private BigDecimal discountPercent;

    @Min(0)
    private Integer stockQuantity;

    @Min(0)
    private Integer minStock;

    private Long categoryId;
    private Boolean active;
}
