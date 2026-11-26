package com.servicio.pedidos.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.servicio.pedidos.client.CatalogClient;
import com.servicio.pedidos.client.CatalogProductResponse;
import com.servicio.pedidos.client.CatalogStockAdjustment;
import com.servicio.pedidos.dto.CreateOrderDTO;
import com.servicio.pedidos.dto.OrderFilter;
import com.servicio.pedidos.dto.OrderItemDTO;
import com.servicio.pedidos.model.Order;
import com.servicio.pedidos.model.OrderItem;
import com.servicio.pedidos.model.OrderStatus;
import com.servicio.pedidos.model.ShippingAddress;
import com.servicio.pedidos.repository.OrderRepository;

import feign.FeignException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final CatalogClient catalogClient;

    /** Crea un pedido con el precio y la disponibilidad obtenidos del catálogo y reserva el stock. */
    @Transactional
    public Order createFromCart(CreateOrderDTO dto) {
        if (dto.getCreationKey() != null) {
            var existing = findByAttempt(dto.getUserId(), dto.getCreationKey());
            if (existing.isPresent()) return existing.get();
        }
        List<CatalogProductResponse> products = new ArrayList<>();
        for (OrderItemDTO item : dto.getItems()) {
            validateItem(item.getQuantity(), item.getProductId());
            CatalogProductResponse product = getCatalogProduct(item.getProductId());
            validateAvailability(product, item.getQuantity());
            products.add(product);
        }

        Order order = new Order();
        order.setUserId(dto.getUserId());
        if (dto.getCreationKey() != null) {
            order.setCreationKey(dto.getUserId() + ":" + dto.getCreationKey());
        }
        order.setShippingAddress(new ShippingAddress(
                dto.getShippingAddress().getStreet().trim(), dto.getShippingAddress().getCity().trim(),
                dto.getShippingAddress().getHouseNumber(), dto.getShippingAddress().getRegion(),
                dto.getShippingAddress().getPostalCode()));
        for (int i = 0; i < dto.getItems().size(); i++) {
            OrderItemDTO requested = dto.getItems().get(i);
            CatalogProductResponse product = products.get(i);
            OrderItem item = new OrderItem(product.getId(), product.getName(), requested.getQuantity(), product.getFinalPrice());
            item.setOrder(order);
            order.getItems().add(item);
        }
        recalculateTotal(order);
        // La restricción única decide qué petición puede reservar stock antes de llamar a Catálogo.
        Order saved = orderRepository.saveAndFlush(order);

        List<ReservedStock> reserved = new ArrayList<>();
        try {
            for (OrderItem item : saved.getItems()) {
                adjustStock(item.getProductId(), -item.getQuantity(), "Reserva para pedido " + saved.getId());
                reserved.add(new ReservedStock(item.getProductId(), item.getQuantity()));
            }
        } catch (RuntimeException ex) {
            restoreStock(reserved, saved.getId());
            throw ex;
        }

        log.info("Pedido creado id={} para usuario {}", saved.getId(), saved.getUserId());
        return saved;
    }

    @Transactional(readOnly = true)
    public Optional<Order> findByAttempt(Long userId, String key) {
        return orderRepository.findByCreationKey(userId + ":" + key);
    }

    @Transactional(readOnly = true)
    public Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new EntityNotFoundException("Pedido no encontrado: " + orderId));
    }

    @Transactional(readOnly = true)
    public List<Order> findByUserId(Long userId) {
        return orderRepository.findByUserId(userId);
    }

    @Transactional(readOnly = true)
    public List<Order> findByStatus(OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public Page<Order> listOrders(OrderFilter filter, Pageable pageable) {
        if (filter.getUserId() != null && filter.getStatus() != null) {
            return orderRepository.findByUserIdAndStatus(filter.getUserId(), filter.getStatus(), pageable);
        }
        if (filter.getUserId() != null) return orderRepository.findByUserId(filter.getUserId(), pageable);
        if (filter.getStatus() != null) return orderRepository.findByStatus(filter.getStatus(), pageable);
        return orderRepository.findAll(pageable);
    }

    @Transactional
    public Order addItem(Long orderId, OrderItemDTO dto) {
        Order order = getOrder(orderId);
        ensureEditable(order);
        validateItem(dto.getQuantity(), dto.getProductId());
        CatalogProductResponse product = getCatalogProduct(dto.getProductId());
        validateAvailability(product, dto.getQuantity());

        OrderItem existing = order.getItems().stream()
                .filter(item -> item.getProductId().equals(dto.getProductId())).findFirst().orElse(null);
        adjustStock(product.getId(), -dto.getQuantity(), "Reserva adicional para pedido " + orderId);
        if (existing == null) {
            existing = new OrderItem(product.getId(), product.getName(), dto.getQuantity(), product.getFinalPrice());
            existing.setOrder(order);
            order.getItems().add(existing);
        } else {
            existing.setQuantity(existing.getQuantity() + dto.getQuantity());
        }
        recalculateTotal(order);
        return orderRepository.save(order);
    }

    @Transactional
    public Order updateItemQuantity(Long orderId, Long productId, Integer newQty) {
        Order order = getOrder(orderId);
        ensureEditable(order);
        if (newQty == null || newQty < 1) {
            throw new IllegalArgumentException("La cantidad mínima es 1; para quitarlo use el endpoint de eliminación");
        }
        OrderItem item = findItem(order, productId);
        int difference = newQty - item.getQuantity();
        if (difference > 0) {
            CatalogProductResponse product = getCatalogProduct(productId);
            validateAvailability(product, difference);
            adjustStock(productId, -difference, "Ajuste de cantidad para pedido " + orderId);
        } else if (difference < 0) {
            adjustStock(productId, -difference, "Devolución de stock por ajuste del pedido " + orderId);
        }
        item.setQuantity(newQty);
        recalculateTotal(order);
        return orderRepository.save(order);
    }

    @Transactional
    public Order removeItem(Long orderId, Long productId) {
        Order order = getOrder(orderId);
        ensureEditable(order);
        OrderItem item = findItem(order, productId);
        adjustStock(productId, item.getQuantity(), "Liberación de stock del pedido " + orderId);
        order.getItems().remove(item);
        recalculateTotal(order);
        return orderRepository.save(order);
    }

    @Transactional
    public Order clearItems(Long orderId) {
        Order order = getOrder(orderId);
        ensureEditable(order);
        for (OrderItem item : new ArrayList<>(order.getItems())) {
            adjustStock(item.getProductId(), item.getQuantity(), "Liberación de stock del pedido " + orderId);
        }
        order.getItems().clear();
        recalculateTotal(order);
        return orderRepository.save(order);
    }

    /** Confirma el pedido cuando pagos informa que el cobro fue exitoso. */
    @Transactional
    public void confirmOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() != OrderStatus.PENDIENTE) {
            if (order.getStatus() == OrderStatus.EN_PREPARACION) return;
            throw new IllegalArgumentException("Solo se puede confirmar un pedido pendiente");
        }
        if (order.getItems().isEmpty()) throw new IllegalArgumentException("El pedido debe tener al menos un producto");
        if (order.getShippingAddress() == null) throw new IllegalArgumentException("La dirección de envío es obligatoria");
        order.setStatus(OrderStatus.EN_PREPARACION);
        orderRepository.save(order);
    }

    @Transactional
    public void updateStatus(Long orderId, OrderStatus newStatus) {
        if (newStatus == null) throw new IllegalArgumentException("El estado es obligatorio");
        if (newStatus == OrderStatus.CANCELADO) {
            cancelOrder(orderId);
            return;
        }
        Order order = getOrder(orderId);
        if (!isValidTransition(order.getStatus(), newStatus)) {
            throw new IllegalArgumentException("Transición de estado no permitida: " + order.getStatus() + " -> " + newStatus);
        }
        order.setStatus(newStatus);
        orderRepository.save(order);
    }

    @Transactional
    public void cancelOrder(Long orderId) {
        Order order = getOrder(orderId);
        if (order.getStatus() == OrderStatus.CANCELADO || order.getStatus() == OrderStatus.ENVIADO
                || order.getStatus() == OrderStatus.ENTREGADO) {
            throw new IllegalArgumentException("No se puede cancelar un pedido en estado " + order.getStatus());
        }
        if (order.getStatus() == OrderStatus.PENDIENTE || order.getStatus() == OrderStatus.EN_PREPARACION) {
            for (OrderItem item : order.getItems()) {
                adjustStock(item.getProductId(), item.getQuantity(), "Devolución por cancelación del pedido " + orderId);
            }
        }
        order.setStatus(OrderStatus.CANCELADO);
        orderRepository.save(order);
    }

    @Transactional(readOnly = true)
    public BigDecimal getSubtotal(OrderItem item) {
        return item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
    }

    private void recalculateTotal(Order order) {
        BigDecimal total = order.getItems().stream().map(this::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(total);
    }

    private void ensureEditable(Order order) {
        if (order.getStatus() != OrderStatus.PENDIENTE) {
            throw new IllegalArgumentException("Solo se puede modificar un pedido pendiente");
        }
    }

    private OrderItem findItem(Order order, Long productId) {
        return order.getItems().stream().filter(item -> item.getProductId().equals(productId)).findFirst()
                .orElseThrow(() -> new EntityNotFoundException("El producto " + productId + " no está en el pedido"));
    }

    private void validateItem(Integer quantity, Long productId) {
        if (productId == null) throw new IllegalArgumentException("El productId es obligatorio");
        if (quantity == null || quantity < 1) throw new IllegalArgumentException("La cantidad mínima es 1");
    }

    private void validateAvailability(CatalogProductResponse product, Integer quantity) {
        if (product == null || !Boolean.TRUE.equals(product.getActive())) {
            throw new IllegalArgumentException("El producto no existe o está inactivo");
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Stock insuficiente para el producto " + product.getId());
        }
        if (product.getFinalPrice() == null || product.getFinalPrice().signum() < 0) {
            throw new IllegalArgumentException("El catálogo devolvió un precio inválido para el producto " + product.getId());
        }
    }

    private CatalogProductResponse getCatalogProduct(Long productId) {
        try {
            return catalogClient.getProduct(productId);
        } catch (FeignException.NotFound ex) {
            throw new EntityNotFoundException("Producto no encontrado en el catálogo: " + productId);
        } catch (FeignException ex) {
            throw new IllegalStateException("No se pudo conectar con el catálogo para consultar el producto " + productId);
        }
    }

    private void adjustStock(Long productId, int quantity, String reason) {
        try {
            catalogClient.adjustStock(productId, new CatalogStockAdjustment(quantity, reason));
        } catch (FeignException.NotFound ex) {
            throw new EntityNotFoundException("Producto no encontrado en el catálogo: " + productId);
        } catch (FeignException ex) {
            throw new IllegalStateException("No se pudo actualizar el stock del producto " + productId + ". Verifique el servicio de catálogo.");
        }
    }

    private void restoreStock(List<ReservedStock> reserved, Long orderId) {
        for (ReservedStock stock : reserved) {
            try {
                adjustStock(stock.productId(), stock.quantity(), "Compensación de reserva fallida del pedido " + orderId);
            } catch (RuntimeException compensationError) {
                log.error("No se pudo devolver stock del producto {} para el pedido {}: {}",
                        stock.productId(), orderId, compensationError.getMessage());
            }
        }
    }

    private boolean isValidTransition(OrderStatus current, OrderStatus target) {
        if (current == target) return true;
        return switch (current) {
            case PENDIENTE -> target == OrderStatus.CANCELADO;
            case EN_PREPARACION -> target == OrderStatus.ENVIADO || target == OrderStatus.CANCELADO;
            case ENVIADO -> target == OrderStatus.ENTREGADO;
            case ENTREGADO, CANCELADO -> false;
        };
    }

    private record ReservedStock(Long productId, Integer quantity) {}
}
