package com.servicio.codigoms_pagos.dto;

import java.math.BigDecimal;

import com.servicio.codigoms_pagos.model.PaymentMethod;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Lo que se le envia a la pasarela
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {
    private Long orderId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod method;
}
