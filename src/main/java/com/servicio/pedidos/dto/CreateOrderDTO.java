package com.servicio.pedidos.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CreateOrderDTO {

    @NotNull(message = "El userId es obligatorio")
    private Long userId;

    @NotEmpty(message = "El pedido debe tener al menos un item")
    @Valid
    private List<OrderItemDTO> items;

    @NotNull(message = "La direccion de envio es obligatoria")
    @Valid
    private ShippingAddressDTO shippingAddress;
}
