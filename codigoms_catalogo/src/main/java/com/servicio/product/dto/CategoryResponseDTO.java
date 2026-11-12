package com.servicio.product.dto;

import lombok.Data;

@Data
public class CategoryResponseDTO {
    private Long id;
    private String name;
    private String description;
    private Boolean active;
}
