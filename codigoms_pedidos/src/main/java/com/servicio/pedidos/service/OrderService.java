package com.servicio.pedidos.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicio.pedidos.dto.CreateOrderDTO;
import com.servicio.pedidos.dto.OrderFilter;
import com.servicio.pedidos.dto.OrderItemDTO;
import com.servicio.pedidos.model.Order;
import com.servicio.pedidos.model.OrderStatus;
import com.servicio.pedidos.model.ShippingAddress;
import com.servicio.pedidos.repository.OrderRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;

    @Transactional
    public Order createFromCart(CreateOrderDTO dto) {
        Order order = new Order();
        order.setUserId(dto.getUserId());
        order.setShippingAddress(new ShippingAddress(
                dto.getShippingAddress().getStreet(), dto.getShippingAddress().getCity()));

        for (OrderItemDTO i : dto.getItems()) {
            order.addItem(i.getProductId(), i.getProductName(), i.getQuantity(), i.getUnitPrice());
        }

        Order saved = orderRepository.save(order);
        log.info("Pedido creado id={} para el usuario {}", saved.getId(), saved.getUserId());
        return saved;
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + orderId));
    }

    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public Page<Order> listOrders(OrderFilter filter, Pageable pageable) {
        if (filter.getUserId() != null && filter.getStatus() != null) {
            return orderRepository.findByUserIdAndStatus(filter.getUserId(), filter.getStatus(), pageable);
        }
        if (filter.getUserId() != null) {
            return orderRepository.findByUserId(filter.getUserId(), pageable);
        }
        if (filter.getStatus() != null) {
            return orderRepository.findByStatus(filter.getStatus(), pageable);
        }
        return orderRepository.findAll(pageable);
    }

    // ---- Carrito (solo pedidos PENDIENTE) ----

    @Transactional
    public Order addItem(Long orderId, OrderItemDTO item) {
        Order order = getOrder(orderId);
        order.addItem(item.getProductId(), item.getProductName(), item.getQuantity(), item.getUnitPrice());
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateItemQuantity(Long orderId, Long productId, Integer newQty) {
        Order order = getOrder(orderId);
        order.updateItemQuantity(productId, newQty);
        return orderRepository.save(order);
    }

    @Transactional
    public Order removeItem(Long orderId, Long productId) {
        Order order = getOrder(orderId);
        order.removeItem(productId);
        return orderRepository.save(order);
    }

    @Transactional
    public Order clearItems(Long orderId) {
        Order order = getOrder(orderId);
        order.clearItems();
        return orderRepository.save(order);
    }

    // ---- Compra y estado ----

    // Lo dispara el aviso de pago exitoso del microservicio de pagos.
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = getOrder(orderId);
        order.confirmOrder();
        orderRepository.save(order);
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus status) {
        Order order = getOrder(orderId);
        order.updateStatus(status);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (!order.cancelOrder()) {
            throw new RuntimeException("No se puede cancelar un pedido en estado " + order.getStatus());
        }
        orderRepository.save(order);
    }
}
