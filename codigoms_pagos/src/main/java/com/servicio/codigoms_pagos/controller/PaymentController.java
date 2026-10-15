package com.servicio.codigoms_pagos.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.codigoms_pagos.dto.PaymentDTO;
import com.servicio.codigoms_pagos.dto.PaymentResponse;
import com.servicio.codigoms_pagos.dto.Receipt;
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
        return ResponseEntity.status(201).body(paymentService.processPayment(dto));
    }

    @GetMapping("/{id}/status")
    public ResponseEntity<PaymentStatus> getPaymentStatus(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.getPaymentStatus(id));
    }

    @PostMapping("/{id}/retry")
    public ResponseEntity<PaymentResponse> retryPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.retryPayment(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<Boolean> cancelPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.cancelPayment(id));
    }

    @PostMapping("/{id}/refund")
    public ResponseEntity<Boolean> refundPayment(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.refundPayment(id));
    }

    @GetMapping("/{id}/receipt")
    public ResponseEntity<Receipt> generateReceipt(@PathVariable Long id) {
        return ResponseEntity.ok(paymentService.generateReceipt(id));
    }

    @GetMapping("/by-order/{orderId}")
    public ResponseEntity<List<PaymentResponse>> findByOrderId(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.findByOrderId(orderId));
    }

    @GetMapping("/by-status/{status}")
    public ResponseEntity<List<PaymentResponse>> findByStatus(@PathVariable PaymentStatus status) {
        return ResponseEntity.ok(paymentService.findByStatus(status));
    }
}
