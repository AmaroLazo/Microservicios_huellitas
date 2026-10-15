package com.servicio.codigoms_pagos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.servicio.codigoms_pagos.dto.GatewayEvent;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String transactionId;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private LocalDateTime transactionDate;

    public Payment(Long orderId, BigDecimal amount, PaymentMethod method, String idempotencyKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.method = method;
        this.idempotencyKey = idempotencyKey;
        this.transactionDate = LocalDateTime.now();
    }

    // ---- Comportamiento del diagrama ----

    public PaymentStatus getPaymentStatus() {
        return status;
    }

    public Boolean validateIdempotency(String key) {
        return idempotencyKey != null && idempotencyKey.equals(key);
    }

    // Aplica el resultado que informa la pasarela (webhook).
    public void handleGatewayResult(GatewayEvent event) {
        this.status = event.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED;
    }

    // Solo se puede cancelar un pago pendiente o fallido. Devuelve true si quedó cancelado.
    public Boolean cancelPayment() {
        if (status == PaymentStatus.PENDING || status == PaymentStatus.FAILED) {
            this.status = PaymentStatus.CANCELLED;
            return true;
        }
        return false;
    }

    // Solo se admite el reembolso total de un pago exitoso. Devuelve true si quedó reembolsado.
    public Boolean refundPayment(BigDecimal refundAmount) {
        if (status != PaymentStatus.SUCCESS || refundAmount == null || refundAmount.compareTo(amount) != 0) {
            return false;
        }
        this.status = PaymentStatus.REFUNDED;
        return true;
    }
}
