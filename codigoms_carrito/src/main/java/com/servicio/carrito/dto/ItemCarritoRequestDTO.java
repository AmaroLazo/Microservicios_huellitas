package com.servicio.carrito.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ItemCarritoRequestDTO {
    @NotNull(message = "El libroId es obligatorio")
    private Long libroId;

    @NotNull
    @Min(value = 1, message = "La cantidad minima es 1")
    private Integer cantidad;
}
