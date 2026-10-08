package com.servicio.pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.servicio.pedidos.model.OrderStatus;

import lombok.Data;

@Data
public class OrderResponse {
    private Long orderId;
    private Long userId;
    private OrderStatus status;
    private BigDecimal total;
    private ShippingAddressDTO shippingAddress;
    private List<OrderItemResponse> items;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
