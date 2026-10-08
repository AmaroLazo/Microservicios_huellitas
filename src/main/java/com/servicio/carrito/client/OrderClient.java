package com.servicio.carrito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import com.servicio.carrito.dto.CreateOrderDTO;
import com.servicio.carrito.dto.OrderResponse;

@FeignClient(name = "codigoms-orders", url = "${orders.service.url}")
public interface OrderClient {

    @PostMapping("/api/orders")
    OrderResponse createOrder(@RequestBody CreateOrderDTO order);
}
