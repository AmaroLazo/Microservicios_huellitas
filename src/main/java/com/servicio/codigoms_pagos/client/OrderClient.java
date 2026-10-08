package com.servicio.codigoms_pagos.client;

import java.math.BigDecimal;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.servicio.codigoms_pagos.dto.OrderResponse;
import com.servicio.codigoms_pagos.model.PaymentStatus;

@FeignClient(name = "codigoms-orders", url = "${orders.service.url}")
public interface OrderClient {

    @GetMapping("/api/orders/{id}")
    OrderResponse getOrder(@PathVariable("id") Long orderId);

    default BigDecimal getOrderAmount(Long orderId) {
        return getOrder(orderId).getTotal();
    }

    @PutMapping("/api/orders/{id}/payment-status")
    void notifyPaymentResult(@PathVariable("id") Long orderId, @RequestParam("status") PaymentStatus status);
}
