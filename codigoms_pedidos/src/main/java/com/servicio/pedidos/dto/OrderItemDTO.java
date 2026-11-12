package com.servicio.pedidos.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class OrderItemDTO {

    @NotNull(message = "El productId es obligatorio")
    @Positive(message = "El productId debe ser positivo")
    private Long productId;

    // Se conserva por compatibilidad con clientes antiguos; pedidos usa el nombre del catálogo.
    private String productName;

    @NotNull
    @Min(value = 1, message = "La cantidad minima es 1")
    private Integer quantity;

    // Se conserva por compatibilidad con clientes antiguos; pedidos usa el precio del catálogo.
    private BigDecimal unitPrice;
}
