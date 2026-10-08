package com.servicio.carrito.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderResponse {
    private Long orderId;
    private String status;
    private BigDecimal total;
}
