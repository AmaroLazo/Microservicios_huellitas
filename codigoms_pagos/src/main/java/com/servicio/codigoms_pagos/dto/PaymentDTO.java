package com.servicio.codigoms_pagos.dto;

import com.servicio.codigoms_pagos.model.PaymentMethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class PaymentDTO {

    @NotNull(message = "El orderId es obligatorio")
    @Positive(message = "El orderId debe ser positivo")
    private Long orderId;

    @NotNull(message = "El metodo de pago es obligatorio")
    private PaymentMethod method;

    @NotBlank(message = "La idempotencyKey es obligatoria")
    private String idempotencyKey;

    // Débito siempre 1 cuota. Crédito: 1, 3, 6, 8 o 12.
    private Integer installments = 1;
}
