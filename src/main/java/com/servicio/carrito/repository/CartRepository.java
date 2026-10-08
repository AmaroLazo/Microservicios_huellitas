package com.servicio.carrito.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.carrito.model.Cart;
import com.servicio.carrito.model.CartStatus;

public interface CartRepository extends JpaRepository<Cart, Long> {

    Optional<Cart> findByUserIdAndStatus(Long userId, CartStatus status);

    Optional<Cart> findBySessionIdAndStatus(String sessionId, CartStatus status);

    default Optional<Cart> findActiveByUserId(Long userId) {
        return findByUserIdAndStatus(userId, CartStatus.ACTIVE);
    }

    default Optional<Cart> findActiveBySessionId(String sessionId) {
        return findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
    }
}
