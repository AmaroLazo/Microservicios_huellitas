package com.servicio.codigoms_pagos.service;

import java.math.BigDecimal;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicio.codigoms_pagos.client.OrderClient;
import com.servicio.codigoms_pagos.client.PaymentGatewayClient;
import com.servicio.codigoms_pagos.dto.GatewayEvent;
import com.servicio.codigoms_pagos.dto.GatewayResponse;
import com.servicio.codigoms_pagos.dto.PaymentDTO;
import com.servicio.codigoms_pagos.dto.PaymentRequest;
import com.servicio.codigoms_pagos.dto.PaymentResponse;
import com.servicio.codigoms_pagos.dto.Receipt;
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

    // El monto se obtiene de la orden, no lo envía el cliente.
    @Transactional
    public PaymentResponse processPayment(PaymentDTO dto) {
        String key = dto.getIdempotencyKey();
        Payment existing = paymentRepository.findByIdempotencyKey(key)
                .filter(p -> p.validateIdempotency(key))
                .orElse(null);
        if (existing != null) {
            return toResponse(existing);
        }

        BigDecimal amount = getOrderAmount(dto.getOrderId());
        Payment payment = new Payment(dto.getOrderId(), amount, dto.getMethod(), key);
        return toResponse(executeTransaction(paymentRepository.save(payment)));
    }

    // Cobra el pago en la pasarela y notifica el resultado a ordenes.
    @Transactional
    public Payment executeTransaction(Payment payment) {
        payment.setStatus(PaymentStatus.PROCESSING);

        GatewayResponse response = gatewayClient.charge(new PaymentRequest(
                payment.getOrderId(), payment.getAmount(), payment.getMethod()));

        payment.setTransactionId(response.getTransactionId());
        if (response.isSuccess()) {
            payment.setStatus(PaymentStatus.SUCCESS);
        } else {
            payment.setStatus(PaymentStatus.FAILED);
            log.warn("Pago {} rechazado por la pasarela: {}", payment.getId(), response.getMessage());
        }

        paymentRepository.save(payment);
        notifyOrder(payment);
        return payment;
    }

    @Transactional(readOnly = true)
    public PaymentStatus getPaymentStatus(Long paymentId) {
        return findPayment(paymentId).getPaymentStatus();
    }

    @Transactional
    public void handleGatewayResult(GatewayEvent event) {
        Payment payment = paymentRepository.findByTransactionId(event.getTransactionId())
                .orElseThrow(() -> new RuntimeException("Pago no encontrado para la transaccion: "
                        + event.getTransactionId()));

        payment.handleGatewayResult(event);
        if (!event.isSuccess()) {
            log.warn("Pago {} fallido segun la pasarela: {}", payment.getId(), event.getFailureReason());
        }
        paymentRepository.save(payment);
        notifyOrder(payment);
    }

    @Transactional
    public PaymentResponse retryPayment(Long paymentId) {
        Payment payment = findPayment(paymentId);
        if (payment.getPaymentStatus() != PaymentStatus.FAILED) {
            throw new RuntimeException("Solo se pueden reintentar pagos fallidos");
        }
        return toResponse(executeTransaction(payment));
    }

    // Devuelve false si el pago ya no se puede cancelar (p. ej. está pagado o en proceso).
    @Transactional
    public Boolean cancelPayment(Long paymentId) {
        Payment payment = findPayment(paymentId);
        Boolean cancelled = payment.cancelPayment();
        if (Boolean.TRUE.equals(cancelled)) {
            paymentRepository.save(payment);
            notifyOrder(payment);
        }
        return cancelled;
    }

    // Reembolso total del pago.
    @Transactional
    public Boolean refundPayment(Long paymentId) {
        Payment payment = findPayment(paymentId);
        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new RuntimeException("Solo se pueden reembolsar pagos exitosos");
        }

        Boolean ok = gatewayClient.refund(payment.getTransactionId());
        if (!Boolean.TRUE.equals(ok)) {
            return false;
        }
        Boolean refunded = payment.refundPayment(payment.getAmount());
        if (Boolean.TRUE.equals(refunded)) {
            paymentRepository.save(payment);
            notifyOrder(payment);
        }
        return refunded;
    }

    @Transactional(readOnly = true)
    public Receipt generateReceipt(Long paymentId) {
        Payment payment = findPayment(paymentId);
        PaymentStatus status = payment.getPaymentStatus();
        if (status != PaymentStatus.SUCCESS && status != PaymentStatus.REFUNDED) {
            throw new RuntimeException("Solo se puede generar comprobante de pagos exitosos");
        }

        Receipt receipt = new Receipt();
        receipt.setPaymentId(payment.getId());
        receipt.setOrderId(payment.getOrderId());
        receipt.setAmount(payment.getAmount());
        receipt.setMethod(payment.getMethod());
        receipt.setStatus(status);
        receipt.setTransactionId(payment.getTransactionId());
        receipt.setTransactionDate(payment.getTransactionDate());
        return receipt;
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId).stream().map(this::toResponse).toList();
    }

    @Transactional(readOnly = true)
    public List<PaymentResponse> findByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream().map(this::toResponse).toList();
    }

    // ---------- helpers ----------

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado: " + paymentId));
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
            orderClient.notifyPaymentResult(payment.getOrderId(), payment.getPaymentStatus());
        } catch (FeignException e) {
            log.error("No se pudo notificar a ordenes el pago {}: {}", payment.getId(), e.getMessage());
        }
    }

    private PaymentResponse toResponse(Payment p) {
        PaymentResponse dto = new PaymentResponse();
        dto.setId(p.getId());
        dto.setOrderId(p.getOrderId());
        dto.setAmount(p.getAmount());
        dto.setMethod(p.getMethod());
        dto.setStatus(p.getPaymentStatus());
        dto.setTransactionId(p.getTransactionId());
        dto.setTransactionDate(p.getTransactionDate());
        return dto;
    }
}
