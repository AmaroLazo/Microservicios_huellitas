package com.servicio.product.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductUpdateRequestDTO {

    @NotBlank private String name;
    private String description;
    private String imageUrl;

    @NotNull @DecimalMin("0.01")
    private BigDecimal price;
}
