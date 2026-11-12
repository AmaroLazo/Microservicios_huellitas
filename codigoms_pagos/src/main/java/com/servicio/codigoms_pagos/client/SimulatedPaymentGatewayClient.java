package com.servicio.codigoms_pagos.client;

import java.util.UUID;

import org.springframework.stereotype.Component;

import com.servicio.codigoms_pagos.dto.GatewayResponse;
import com.servicio.codigoms_pagos.dto.PaymentRequest;

// Pasarela simulada: aprueba todos los cobros y reembolsos
@Component
public class SimulatedPaymentGatewayClient implements PaymentGatewayClient {

    @Override
    public GatewayResponse charge(PaymentRequest request) {
        return new GatewayResponse(true, "SIM-" + UUID.randomUUID(), "Pago aprobado");
    }

    @Override
    public Boolean refund(String transactionId) {
        return true;
    }
}
