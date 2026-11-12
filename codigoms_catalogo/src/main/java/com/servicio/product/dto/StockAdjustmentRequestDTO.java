package com.servicio.product.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class StockAdjustmentRequestDTO {

    // Positivo = entrada, negativo = salida.
    @NotNull
    private Integer quantity;

    @NotBlank
    private String reason;
}
