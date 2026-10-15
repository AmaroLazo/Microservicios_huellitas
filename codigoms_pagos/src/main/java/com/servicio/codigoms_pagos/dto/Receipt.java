package com.servicio.codigoms_pagos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.servicio.codigoms_pagos.model.PaymentMethod;
import com.servicio.codigoms_pagos.model.PaymentStatus;

import lombok.Data;

// Comprobante de un pago (generateReceipt)
@Data
public class Receipt {
    private Long paymentId;
    private Long orderId;
    private BigDecimal amount;
    private PaymentMethod method;
    private PaymentStatus status;
    private String transactionId;
    private LocalDateTime transactionDate;
}
