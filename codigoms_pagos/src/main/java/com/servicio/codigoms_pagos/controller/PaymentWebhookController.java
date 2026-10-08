package com.servicio.codigoms_pagos.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.codigoms_pagos.dto.GatewayEvent;
import com.servicio.codigoms_pagos.service.PaymentService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/webhooks")
@RequiredArgsConstructor
public class PaymentWebhookController {

    private final PaymentService paymentService;

    @PostMapping("/payments")
    public ResponseEntity<Void> handleGatewayEvent(@RequestBody GatewayEvent event) {
        paymentService.handleGatewayResult(event);
        return ResponseEntity.noContent().build();
    }
}
