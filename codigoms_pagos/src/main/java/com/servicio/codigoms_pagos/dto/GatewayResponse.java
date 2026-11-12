package com.servicio.codigoms_pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo que responde la pasarela
@Data
@NoArgsConstructor
@AllArgsConstructor
public class GatewayResponse {
    private boolean success;
    private String transactionId;
    private String message;
}
