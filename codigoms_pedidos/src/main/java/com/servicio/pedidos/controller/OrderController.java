package com.servicio.pedidos.controller;

import java.util.List;
import org.springframework.dao.DataIntegrityViolationException;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.pedidos.dto.CreateOrderDTO;
import com.servicio.pedidos.dto.OrderFilter;
import com.servicio.pedidos.dto.OrderItemDTO;
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
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody CreateOrderDTO dto,
            @RequestAttribute("huellitasUserId") Long userId) {
        if (!userId.equals(dto.getUserId())) return ResponseEntity.status(403).build();
        try {
            return ResponseEntity.status(201).body(toResponse(orderService.createFromCart(dto)));
        } catch (DataIntegrityViolationException error) {
            if (dto.getCreationKey() == null) throw error;
            return orderService.findByAttempt(userId, dto.getCreationKey())
                    .map(order -> ResponseEntity.ok(toResponse(order)))
                    .orElseThrow(() -> error);
        }
    }

    @GetMapping("/intentos/{key}")
    public ResponseEntity<OrderResponse> getByAttempt(@PathVariable String key,
            @RequestAttribute("huellitasUserId") Long userId) {
        return orderService.findByAttempt(userId, key)
                .map(order -> ResponseEntity.ok(toResponse(order)))
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(orderService.getOrder(id)));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> findByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(orderService.findByUserId(userId).stream()
                .map(this::toResponse).toList());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<OrderResponse>> findByStatus(@PathVariable OrderStatus status) {
        return ResponseEntity.ok(orderService.findByStatus(status).stream()
                .map(this::toResponse).toList());
    }

    @GetMapping
    public ResponseEntity<Page<OrderResponse>> listOrders(OrderFilter filter, Pageable pageable) {
        return ResponseEntity.ok(orderService.listOrders(filter, pageable).map(this::toResponse));
    }

    // ---- Carrito (solo pedidos PENDIENTE) ----

    @PostMapping("/{id}/items")
    public ResponseEntity<OrderResponse> addItem(@PathVariable Long id, @Valid @RequestBody OrderItemDTO item) {
        return ResponseEntity.ok(toResponse(orderService.addItem(id, item)));
    }

    @PutMapping("/{id}/items/{productId}")
    public ResponseEntity<OrderResponse> updateItemQuantity(@PathVariable Long id,
            @PathVariable Long productId, @RequestParam Integer quantity) {
        return ResponseEntity.ok(toResponse(orderService.updateItemQuantity(id, productId, quantity)));
    }

    @DeleteMapping("/{id}/items/{productId}")
    public ResponseEntity<OrderResponse> removeItem(@PathVariable Long id, @PathVariable Long productId) {
        return ResponseEntity.ok(toResponse(orderService.removeItem(id, productId)));
    }

    @DeleteMapping("/{id}/items")
    public ResponseEntity<OrderResponse> clearItems(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(orderService.clearItems(id)));
    }

    // ---- Estado ----

    @PutMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelOrder(@PathVariable Long id) {
        orderService.cancelOrder(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(@PathVariable Long id,
            @RequestParam OrderStatus status) {
        orderService.updateStatus(id, status);
        return ResponseEntity.ok(toResponse(orderService.getOrder(id)));
    }

    // Lo llama el microservicio de pagos
    @PutMapping("/{id}/payment-status")
    public ResponseEntity<Void> updatePaymentStatus(@PathVariable Long id, @RequestParam String status) {
        if (status == null) {
            throw new IllegalArgumentException("El estado del pago es obligatorio");
        }
        switch (status.toUpperCase()) {
            case "SUCCESS" -> orderService.confirmOrder(id);
            case "REFUNDED" -> orderService.cancelOrder(id);
            case "FAILED", "PENDING", "PROCESSING", "CANCELLED" -> { /* El pedido conserva su estado actual. */ }
            default -> throw new IllegalArgumentException("Estado de pago no reconocido: " + status);
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
            a.setHouseNumber(o.getShippingAddress().getHouseNumber());
            a.setRegion(o.getShippingAddress().getRegion());
            a.setPostalCode(o.getShippingAddress().getPostalCode());
            dto.setShippingAddress(a);
        }

        dto.setItems(o.getItems().stream().map(i -> {
            OrderItemResponse r = new OrderItemResponse();
            r.setId(i.getId());
            r.setProductId(i.getProductId());
            r.setProductName(i.getProductName());
            r.setQuantity(i.getQuantity());
            r.setUnitPrice(i.getUnitPrice());
            r.setSubtotal(orderService.getSubtotal(i));
            return r;
        }).toList());
        return dto;
    }
}
