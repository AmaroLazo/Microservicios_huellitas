package com.servicio.codigoms_pagos.dto;

import com.servicio.codigoms_pagos.model.PaymentMethod;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PaymentDTO {

    @NotNull(message = "El orderId es obligatorio")
    private Long orderId;

    @NotNull(message = "El metodo de pago es obligatorio")
    private PaymentMethod method;

    private String currency = "CLP";

    @NotBlank(message = "La idempotencyKey es obligatoria")
    private String idempotencyKey;
}
