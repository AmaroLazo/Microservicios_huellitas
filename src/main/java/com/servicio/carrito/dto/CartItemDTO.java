package com.servicio.carrito.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartItemDTO {

    @NotNull(message = "El productId es obligatorio")
    private Long productId;

    @NotBlank(message = "El productName es obligatorio")
    private String productName;

    @NotNull
    @Min(value = 1, message = "La cantidad minima es 1")
    private Integer quantity;
}
