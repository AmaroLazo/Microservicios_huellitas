package com.servicio.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryRequestDTO {

    @NotBlank(message = "El nombre de la categoría es obligatorio")
    @Size(max = 100, message = "El nombre admite hasta 100 caracteres")
    private String name;

    @Size(max = 255, message = "La descripción admite hasta 255 caracteres")
    private String description;

    private Boolean active;
}
