package com.servicio.codigoms_pagos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

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

    @Column(nullable = false, length = 10)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentStatus status = PaymentStatus.PENDING;

    private String transactionId;

    private String failureReason;

    @Column(nullable = false, unique = true)
    private String idempotencyKey;

    private LocalDateTime transactionDate;

    public Payment(Long orderId, BigDecimal amount, String currency, PaymentMethod method,
            String idempotencyKey) {
        this.orderId = orderId;
        this.amount = amount;
        this.currency = currency;
        this.method = method;
        this.idempotencyKey = idempotencyKey;
        this.transactionDate = LocalDateTime.now();
    }
}
