package com.servicio.codigoms_pagos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.codigoms_pagos.dto.PaymentDTO;
import com.servicio.codigoms_pagos.dto.PaymentResponse;
import com.servicio.codigoms_pagos.model.Payment;
import com.servicio.codigoms_pagos.model.PaymentStatus;
import com.servicio.codigoms_pagos.service.PaymentService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping
    public ResponseEntity<PaymentResponse> processPayment(@Valid @RequestBody PaymentDTO dto) {
        return ResponseEntity.status(201).body(toResponse(paymentService.executeTransaction(dto)));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<PaymentStatus> getPaymentStatus(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPayment(id).getStatus());
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<PaymentResponse> retryPayment(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(paymentService.retryPayment(id)));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Boolean> refundPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refund(id));
    }

    private PaymentResponse toResponse(Payment p) {
        PaymentResponse dto = new PaymentResponse();
        dto.setId(p.getId());
        dto.setOrderId(p.getOrderId());
        dto.setAmount(p.getAmount());
        dto.setCurrency(p.getCurrency());
        dto.setMethod(p.getMethod());
        dto.setStatus(p.getStatus());
        dto.setTransactionId(p.getTransactionId());
        dto.setFailureReason(p.getFailureReason());
        dto.setTransactionDate(p.getTransactionDate());
        return dto;
    }
}
