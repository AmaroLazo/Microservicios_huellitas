package com.servicio.codigoms_pagos.dto;

import lombok.Data;

// Evento que la pasarela envia al webhook
@Data
public class GatewayEvent {
    private String transactionId;
    private boolean success;
    private String failureReason;
}
