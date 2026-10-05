package com.servicio.pedidos.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PedidoRequestDTO {
    @NotNull(message = "El libroId es obligatorio")
    private Long libroId;

    @NotBlank(message = "El cliente es obligatorio")
    private String cliente;

    @NotNull @Min(value = 1, message = "La cantidad minima es 1")
    private Integer cantidad;
}
