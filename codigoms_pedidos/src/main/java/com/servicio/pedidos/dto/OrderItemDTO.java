package com.servicio.pedidos.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OrderItemDTO {

    @NotNull(message = "El productId es obligatorio")
    private Long productId;

    @NotBlank(message = "El productName es obligatorio")
    private String productName;

    @NotNull
    @Min(value = 1, message = "La cantidad minima es 1")
    private Integer quantity;

    @NotNull(message = "El unitPrice es obligatorio")
    @DecimalMin("0.0")
    private BigDecimal unitPrice;
}
