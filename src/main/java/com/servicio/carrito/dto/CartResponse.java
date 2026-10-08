package com.servicio.carrito.dto;

import java.math.BigDecimal;
import java.util.List;

import com.servicio.carrito.model.CartStatus;

import lombok.Data;

@Data
public class CartResponse {
    private Long id;
    private Long userId;
    private CartStatus status;
    private List<CartItemResponse> items;
    private int totalItems;
    private BigDecimal total;
}
