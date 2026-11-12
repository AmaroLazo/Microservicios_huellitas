package com.servicio.codigoms_pagos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.codigoms_pagos.model.Payment;
import com.servicio.codigoms_pagos.model.PaymentStatus;

public interface PaymentRepository extends JpaRepository<Payment, Long> {

    List<Payment> findByOrderId(Long orderId);

    List<Payment> findByStatus(PaymentStatus status);

    Optional<Payment> findByIdempotencyKey(String idempotencyKey);

    Optional<Payment> findByTransactionId(String transactionId);
}
