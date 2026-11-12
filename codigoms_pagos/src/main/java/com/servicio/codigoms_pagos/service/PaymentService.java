package com.servicio.codigoms_pagos.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;

import jakarta.persistence.EntityNotFoundException;
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
import com.servicio.codigoms_pagos.model.PaymentMethod;
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

    /** Obtiene el total real del pedido y procesa el pago una sola vez por clave idempotente. */
    @Transactional
    public PaymentResponse processPayment(PaymentDTO dto) {
        if (dto.getOrderId() == null || dto.getOrderId() <= 0) throw new IllegalArgumentException("El orderId debe ser positivo");
        if (dto.getMethod() == null) throw new IllegalArgumentException("El método de pago es obligatorio");
        if (dto.getIdempotencyKey() == null || dto.getIdempotencyKey().isBlank()) {
            throw new IllegalArgumentException("La idempotencyKey es obligatoria");
        }

        String key = dto.getIdempotencyKey().trim();
        Payment existing = paymentRepository.findByIdempotencyKey(key).orElse(null);
        if (existing != null) {
            if (!existing.getOrderId().equals(dto.getOrderId())) {
                throw new IllegalArgumentException("La clave idempotente ya se utilizó con otro pedido");
            }
            return toResponse(existing);
        }

        int installments = dto.getInstallments() == null ? 1 : dto.getInstallments();
        if (dto.getMethod() == PaymentMethod.DEBITO && installments != 1) {
            throw new IllegalArgumentException("Débito solo permite una cuota");
        }
        BigDecimal factor = switch (installments) {
            case 1 -> new BigDecimal("1.00");
            case 3 -> new BigDecimal("1.03");
            case 6 -> new BigDecimal("1.06");
            case 8 -> new BigDecimal("1.09");
            case 12 -> new BigDecimal("1.12");
            default -> throw new IllegalArgumentException("Cantidad de cuotas no válida");
        };
        BigDecimal amount = getOrderAmount(dto.getOrderId()).multiply(factor).setScale(0, RoundingMode.HALF_UP);
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("El pedido debe tener un total mayor que cero para pagar");
        }
        Payment payment = new Payment();
        payment.setOrderId(dto.getOrderId());
        payment.setAmount(amount);
        payment.setInstallments(installments);
        payment.setMethod(dto.getMethod());
        payment.setIdempotencyKey(key);
        payment.setStatus(PaymentStatus.PROCESSING);
        payment.setTransactionDate(LocalDateTime.now());
        Payment saved = paymentRepository.save(payment);
        return toResponse(executeTransaction(saved));
    }

    /** Ejecuta el cobro y notifica su resultado al microservicio de pedidos. */
    @Transactional
    public Payment executeTransaction(Payment payment) {
        GatewayResponse response = gatewayClient.charge(new PaymentRequest(
                payment.getOrderId(), payment.getAmount(), payment.getMethod()));
        if (response == null || response.getTransactionId() == null || response.getTransactionId().isBlank()) {
            payment.setStatus(PaymentStatus.FAILED);
            paymentRepository.save(payment);
            notifyOrder(payment);
            return payment;
        }
        payment.setTransactionId(response.getTransactionId());
        payment.setStatus(response.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setTransactionDate(LocalDateTime.now());
        if (!response.isSuccess()) {
            log.warn("Pago {} rechazado por la pasarela: {}", payment.getId(), response.getMessage());
        }
        paymentRepository.save(payment);
        notifyOrder(payment);
        return payment;
    }

    @Transactional(readOnly = true)
    public PaymentStatus getPaymentStatus(Long paymentId) {
        return findPayment(paymentId).getStatus();
    }

    /** Procesa un webhook repetible: un pago que ya tiene resultado terminal no se modifica de nuevo. */
    @Transactional
    public void handleGatewayResult(GatewayEvent event) {
        if (event == null || event.getTransactionId() == null || event.getTransactionId().isBlank()) {
            throw new IllegalArgumentException("El transactionId del webhook es obligatorio");
        }
        Payment payment = paymentRepository.findByTransactionId(event.getTransactionId())
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado para la transacción: " + event.getTransactionId()));
        if (payment.getStatus() == PaymentStatus.SUCCESS || payment.getStatus() == PaymentStatus.FAILED
                || payment.getStatus() == PaymentStatus.REFUNDED || payment.getStatus() == PaymentStatus.CANCELLED) {
            return;
        }
        payment.setStatus(event.isSuccess() ? PaymentStatus.SUCCESS : PaymentStatus.FAILED);
        payment.setTransactionDate(LocalDateTime.now());
        paymentRepository.save(payment);
        notifyOrder(payment);
    }

    @Transactional
    public PaymentResponse retryPayment(Long paymentId) {
        Payment payment = findPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.FAILED) {
            throw new IllegalArgumentException("Solo se pueden reintentar pagos fallidos");
        }
        payment.setStatus(PaymentStatus.PROCESSING);
        paymentRepository.save(payment);
        return toResponse(executeTransaction(payment));
    }

    @Transactional
    public Boolean cancelPayment(Long paymentId) {
        Payment payment = findPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.PENDING && payment.getStatus() != PaymentStatus.FAILED) {
            return false;
        }
        payment.setStatus(PaymentStatus.CANCELLED);
        paymentRepository.save(payment);
        notifyOrder(payment);
        return true;
    }

    @Transactional
    public Boolean refundPayment(Long paymentId) {
        Payment payment = findPayment(paymentId);
        if (payment.getStatus() != PaymentStatus.SUCCESS) {
            throw new IllegalArgumentException("Solo se pueden reembolsar pagos exitosos");
        }
        Boolean refundedByGateway = gatewayClient.refund(payment.getTransactionId());
        if (!Boolean.TRUE.equals(refundedByGateway)) return false;
        payment.setStatus(PaymentStatus.REFUNDED);
        paymentRepository.save(payment);
        notifyOrder(payment);
        return true;
    }

    @Transactional(readOnly = true)
    public Receipt generateReceipt(Long paymentId) {
        Payment payment = findPayment(paymentId);
        PaymentStatus status = payment.getStatus();
        if (status != PaymentStatus.SUCCESS && status != PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException("Solo se puede generar comprobante de pagos exitosos");
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

    private Payment findPayment(Long paymentId) {
        return paymentRepository.findById(paymentId)
                .orElseThrow(() -> new EntityNotFoundException("Pago no encontrado: " + paymentId));
    }

    private BigDecimal getOrderAmount(Long orderId) {
        try {
            var order = orderClient.getOrder(orderId);
            if (order == null || order.getTotal() == null) {
                throw new IllegalArgumentException("La orden no tiene un total válido");
            }
            if (!"PENDIENTE".equalsIgnoreCase(order.getStatus())) {
                throw new IllegalArgumentException("Solo se puede pagar un pedido pendiente");
            }
            return order.getTotal();
        } catch (FeignException.NotFound ex) {
            throw new EntityNotFoundException("La orden " + orderId + " no existe en el sistema");
        } catch (FeignException ex) {
            throw new IllegalStateException("No se pudo consultar el pedido. Compruebe que el servicio de pedidos esté ejecutándose.");
        }
    }

    private void notifyOrder(Payment payment) {
        try {
            orderClient.notifyPaymentResult(payment.getOrderId(), payment.getStatus());
        } catch (FeignException | IllegalStateException ex) {
            log.error("No se pudo notificar al pedido {} sobre el pago {}: {}",
                    payment.getOrderId(), payment.getId(), ex.getMessage());
        }
    }

    private PaymentResponse toResponse(Payment payment) {
        PaymentResponse dto = new PaymentResponse();
        dto.setId(payment.getId());
        dto.setOrderId(payment.getOrderId());
        dto.setAmount(payment.getAmount());
        dto.setInstallments(payment.getInstallments() == null ? 1 : payment.getInstallments());
        dto.setMethod(payment.getMethod());
        dto.setStatus(payment.getStatus());
        dto.setTransactionId(payment.getTransactionId());
        dto.setTransactionDate(payment.getTransactionDate());
        return dto;
    }
}
