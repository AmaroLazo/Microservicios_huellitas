package com.servicio.codigoms_pagos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.servicio.codigoms_pagos.model.PaymentMethod;
import com.servicio.codigoms_pagos.model.PaymentStatus;

import lombok.Data;

@Data
public class PaymentResponse {
    private Long id;
    private Long orderId;
    private BigDecimal amount;
    private String currency;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private String failureReason;
    private LocalDateTime transactionDate;
}
