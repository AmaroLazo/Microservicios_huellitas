package com.servicio.pedidos.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class OrderItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private Integer quantity;
    private BigDecimal unitPrice;
    private BigDecimal subtotal;
}
