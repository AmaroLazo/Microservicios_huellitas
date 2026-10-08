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
import com.servicio.pedidos.model.OrderItem;
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
        order.setStatus(OrderStatus.PENDIENTE);
        order.setShippingAddress(new ShippingAddress(
                dto.getShippingAddress().getStreet(), dto.getShippingAddress().getCity()));

        for (OrderItemDTO i : dto.getItems()) {
            order.addItem(new OrderItem(i.getProductId(), i.getProductName(),
                    i.getQuantity(), i.getUnitPrice()));
        }
        order.setTotalAmount(order.calculateTotal());

        Order saved = orderRepository.save(order);
        log.info("Pedido creado id={} para el usuario {}", saved.getId(), saved.getUserId());
        return saved;
    }

    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + orderId));
    }

    public List<Order> getOrdersByUser(Long userId) {
        return orderRepository.findByUserId(userId);
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

    @Transactional
    public void confirmPayment(Long orderId) {
        Order order = getOrder(orderId);
        // Si ya no esta pendiente (por ejemplo, aviso repetido) no se hace nada
        if (order.getStatus() == OrderStatus.PENDIENTE) {
            order.setStatus(OrderStatus.EN_PREPARACION);
            orderRepository.save(order);
        }
    }

    @Transactional
    public void changeStatus(Long orderId, OrderStatus status) {
        Order order = getOrder(orderId);
        order.setStatus(status);
        orderRepository.save(order);
    }

    @Transactional
    public void cancel(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() == OrderStatus.ENVIADO
                || order.getStatus() == OrderStatus.ENTREGADO
                || order.getStatus() == OrderStatus.CANCELADO) {
            throw new RuntimeException("No se puede cancelar un pedido en estado " + order.getStatus());
        }
        order.setStatus(OrderStatus.CANCELADO);
        orderRepository.save(order);
    }
}
