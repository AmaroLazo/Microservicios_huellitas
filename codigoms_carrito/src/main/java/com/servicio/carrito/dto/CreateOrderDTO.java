package com.servicio.carrito.dto;

import java.util.List;

import lombok.Data;

@Data
public class CreateOrderDTO {
    private Long userId;
    private List<OrderItemDTO> items;
    private ShippingAddressDTO shippingAddress;
}
