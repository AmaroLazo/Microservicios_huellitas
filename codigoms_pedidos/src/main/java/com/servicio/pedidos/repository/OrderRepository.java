package com.servicio.pedidos.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.servicio.pedidos.model.Order;
import com.servicio.pedidos.model.OrderStatus;

public interface OrderRepository extends JpaRepository<Order, Long> {

    boolean existsBySeedKey(String seedKey);

    Optional<Order> findByCreationKey(String creationKey);

    List<Order> findByUserId(Long userId);

    List<Order> findByStatus(OrderStatus status);

    Page<Order> findByUserId(Long userId, Pageable pageable);

    Page<Order> findByStatus(OrderStatus status, Pageable pageable);

    Page<Order> findByUserIdAndStatus(Long userId, OrderStatus status, Pageable pageable);
}
