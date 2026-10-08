package com.servicio.codigoms_pagos.service;

import java.math.BigDecimal;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicio.codigoms_pagos.client.OrderClient;
import com.servicio.codigoms_pagos.client.PaymentGatewayClient;
import com.servicio.codigoms_pagos.dto.GatewayEvent;
import com.servicio.codigoms_pagos.dto.GatewayResponse;
import com.servicio.codigoms_pagos.dto.PaymentDTO;
import com.servicio.codigoms_pagos.dto.PaymentRequest;
import com.servicio.codigoms_pagos.model.Payment;
import com.servicio.codigoms_pagos.model.PaymentStatus;
import com.servicio.codigoms_pagos.repository.PaymentRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final PaymentGatewayClient gatewayClient;
    private final OrderClient orderClient;

    @Transactional
    public Payment executeTransaction(PaymentDTO dto) {
        Optional<Payment> existing = paymentRepository.findByIdempotencyKey(dto.getIdempotencyKey());
        if (existing.isPresent()) {
            return existing.get();
        }

        BigDecimal amount = getOrderAmount(dto.getOrderId());
        Payment payment = new Payment(dto.getOrderId(), amount, dto.getCurrency(),
                dto.getMethod(), dto.getIdempotencyKey());
        return charge(paymentRepository.save(payment));
    }

    public Payment getPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + paymentId));
    }

    @Transactional
    public Payment retryPayment(Long paymentId) {
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new RuntimeException("Solo se pueden reintentar pagos fallidos");
        }
        payment.setFailureReason(null);
        return charge(payment);
    }

    @Transactional
    public void handleGatewayResult(GatewayEvent event) {
        Payment payment = paymentRepository.findByTransactionId(event.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Pago no encontrado para la transaccion: "
                        + event.getTransactionId()));

        if (event.isSuccess()) {
            payment.setStatus(PaymentStatus.SUCCESS);
            payment.setFailureReason(null);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(event.getFailureReason());
        }
        paymentRepository.save(payment);
        notifyOrder(payment);
    }

    @Transactional
    public Boolean refund(Long paymentId) {
        Payment payment = getPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Solo se pueden reembolsar pagos exitosos");
        }

        Boolean ok = gatewayClient.refund(payment.getTransactionId());
        if (Boolean.TRUE.equals(ok)) {
            payment.setStatus(PaymentStatus.REFUNDED);
            paymentRepository.save(payment);
            notifyOrder(payment);
        }
        return ok;
    }

    // ---------- helpers ----------

    private Payment charge(Payment payment) {
        payment.setStatus(PaymentStatus.PROCESSING);

        GatewayResponse response = gatewayClient.charge(new PaymentRequest(
                payment.getOrderId(), payment.getAmount(), payment.getCurrency(), payment.getMethod()));

        payment.setTransactionId(response.getTransactionId());
        if (response.isSuccess()) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            payment.setFailureReason(response.getMessage());
        }

        paymentRepository.save(payment);
        notifyOrder(payment);
        return payment;
    }

    private BigDecimal getOrderAmount(Long orderId) {
        try {
            return orderClient.getOrderAmount(orderId);
        } catch (FeignException.NotFound e) {
            throw new RuntimeException("La orden " + orderId + " no existe en el sistema");
        } catch (FeignException e) {
            throw new RuntimeException("No se pudo verificar la orden. Verifique que el servicio de ordenes este corriendo.");
        }
    }

    private void notifyOrder(Payment payment) {
        try {
            orderClient.notifyPaymentResult(payment.getOrderId(), payment.getStatus());
        } catch (FeignException e) {
            log.error("No se pudo notificar a ordenes el pago {}: {}", payment.getId(), e.getMessage());
        }
    }
}
