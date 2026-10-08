package com.servicio.carrito.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.servicio.carrito.dto.CartItemDTO;
import com.servicio.carrito.dto.CartItemResponse;
import com.servicio.carrito.dto.CartResponse;
import com.servicio.carrito.dto.OrderResponse;
import com.servicio.carrito.dto.ShippingAddressDTO;
import com.servicio.carrito.model.Cart;
import com.servicio.carrito.service.CartService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;

    @GetMapping
    public ResponseEntity<CartResponse> getCart(@RequestParam Long userId) {
        return ResponseEntity.ok(toResponse(cartService.getCart(userId)));
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse> addItem(@RequestParam Long userId,
            @Valid @RequestBody CartItemDTO dto) {
        return ResponseEntity.ok(toResponse(cartService.addItem(userId, dto)));
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> updateQuantity(@RequestParam Long userId,
            @PathVariable Long itemId, @RequestParam int quantity) {
        return ResponseEntity.ok(toResponse(cartService.updateQuantity(userId, itemId, quantity)));
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse> removeItem(@RequestParam Long userId, @PathVariable Long itemId) {
        return ResponseEntity.ok(toResponse(cartService.removeItem(userId, itemId)));
    }

    @DeleteMapping
    public ResponseEntity<Void> clearCart(@RequestParam Long userId) {
        cartService.clearCart(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/merge")
    public ResponseEntity<CartResponse> mergeGuestCart(@RequestParam String sessionId,
            @RequestParam Long userId) {
        return ResponseEntity.ok(toResponse(cartService.mergeCarts(sessionId, userId)));
    }

    @PostMapping("/checkout")
    public ResponseEntity<OrderResponse> checkout(@RequestParam Long userId,
            @Valid @RequestBody ShippingAddressDTO address) {
        return ResponseEntity.ok(cartService.checkout(userId, address));
    }

    private CartResponse toResponse(Cart cart) {
        CartResponse dto = new CartResponse();
        dto.setId(cart.getId());
        dto.setUserId(cart.getUserId());
        dto.setStatus(cart.getStatus());
        dto.setItems(cart.getItems().stream().map(i -> {
            CartItemResponse r = new CartItemResponse();
            r.setId(i.getId());
            r.setProductId(i.getProductId());
            r.setProductName(i.getProductName());
            r.setQuantity(i.getQuantity());
            r.setUnitPrice(i.getUnitPrice());
            r.setSubtotal(i.getSubtotal());
            return r;
        }).toList());
        dto.setTotalItems(cart.getTotalItems());
        dto.setTotal(cart.calculateTotal());
        return dto;
    }
}
