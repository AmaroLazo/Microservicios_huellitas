package com.servicio.pedidos.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CreateOrderDTO {

    @NotNull(message = "El userId es obligatorio")
    @Positive(message = "El userId debe ser positivo")
    private Long userId;

    @Pattern(regexp = "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}",
            message = "La clave de creación debe ser un UUID válido")
    private String creationKey;

    @NotEmpty(message = "El pedido debe tener al menos un item")
    @Valid
    private List<OrderItemDTO> items;

    @NotNull(message = "La direccion de envio es obligatoria")
    @Valid
    private ShippingAddressDTO shippingAddress;
}
