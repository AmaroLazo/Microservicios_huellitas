package com.servicio.pedidos.controller;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.pedidos.dto.CreateOrderDTO;
import com.servicio.pedidos.dto.OrderFilter;
import com.servicio.pedidos.dto.OrderItemResponse;
import com.servicio.pedidos.dto.OrderResponse;
import com.servicio.pedidos.dto.ShippingAddressDTO;
import com.servicio.pedidos.model.Order;
import com.servicio.pedidos.model.OrderStatus;
import com.servicio.pedidos.service.OrderService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/orders")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderDTO dto) {
        return ResponseEntity.status(201).body(toResponse(orderService.createFromCart(dto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(toResponse(orderService.getOrder(id)));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getMyOrders(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.getOrdersByUser(userId).stream()
                .map(this::toResponse).toList());
    }

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancel(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listOrders(OrderFilter filter, Pageable pageable) {
        return ResponseEntity.ok(orderService.listOrders(filter, pageable).map(this::toResponse));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
            @RequestParam OrderStatus status) {
        orderService.changeStatus(id, status);
        return ResponseEntity.ok(toResponse(orderService.getOrder(id)));
    }

    // Lo llama el microservicio de pagos
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<Void> updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        if ("SUCCESS".equals(status)) {
            orderService.confirmPayment(id);
        } else if ("REFUNDED".equals(status)) {
            orderService.cancel(id);
        }
        return ResponseEntity.noContent().build();
    }

    private OrderResponse toResponse(Order o) {
        OrderResponse dto = new OrderResponse();
        dto.setOrderId(o.getId());
        dto.setUserId(o.getUserId());
        dto.setStatus(o.getStatus());
        dto.setTotal(o.getTotalAmount());
        dto.setCreatedAt(o.getCreatedAt());
        dto.setUpdatedAt(o.getUpdatedAt());

        if (o.getShippingAddress() != null) {
            ShippingAddressDTO a = new ShippingAddressDTO();
            a.setStreet(o.getShippingAddress().getStreet());
            a.setCity(o.getShippingAddress().getCity());
            dto.setShippingAddress(a);
        }

        dto.setItems(o.getItems().stream().map(i -> {
            OrderItemResponse r = new OrderItemResponse();
            r.setId(i.getId());
            r.setProductId(i.getProductId());
            r.setProductName(i.getProductName());
            r.setQuantity(i.getQuantity());
            r.setUnitPrice(i.getUnitPrice());
            r.setSubtotal(i.getSubtotal());
            return r;
        }).toList());
        return dto;
    }
}
