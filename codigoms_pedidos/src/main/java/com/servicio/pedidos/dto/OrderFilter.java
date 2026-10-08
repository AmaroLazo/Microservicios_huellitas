package com.servicio.pedidos.dto;

import com.servicio.pedidos.model.OrderStatus;

import lombok.Data;

@Data
public class OrderFilter {
    private OrderStatus status;
    private Long userId;
}
