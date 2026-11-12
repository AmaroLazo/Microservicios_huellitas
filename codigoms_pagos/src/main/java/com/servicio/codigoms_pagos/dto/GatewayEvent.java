package com.servicio.codigoms_pagos.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class GatewayEvent {
    @NotBlank(message = "El transactionId es obligatorio")
    private String transactionId;
    private boolean success;
    private String failureReason;
}
