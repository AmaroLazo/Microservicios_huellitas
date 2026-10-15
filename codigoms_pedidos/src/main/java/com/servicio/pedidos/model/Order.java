package com.servicio.pedidos.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status = OrderStatus.PENDIENTE;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @Embedded
    private ShippingAddress shippingAddress;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private List<OrderItem> items = new ArrayList<>();

    @PrePersist
    void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // ---- Carrito: solo se puede modificar mientras el pedido está PENDIENTE ----

    // Si el producto ya está en el pedido se suma la cantidad (se conserva el precio original).
    public void addItem(Long productId, String productName, Integer quantity, BigDecimal price) {
        ensureEditable();
        if (quantity == null || quantity < 1) {
            throw new RuntimeException("La cantidad minima es 1");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new RuntimeException("El precio no puede ser negativo");
        }

        OrderItem existing = items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);
        if (existing != null) {
            existing.setQuantity(existing.getQuantity() + quantity);
        } else {
            OrderItem item = new OrderItem(productId, productName, quantity, price);
            item.setOrder(this);
            items.add(item);
        }
        this.totalAmount = calculateTotal();
    }

    public void updateItemQuantity(Long productId, Integer newQty) {
        ensureEditable();
        if (newQty == null || newQty < 1) {
            throw new RuntimeException("La cantidad minima es 1 (para quitar el producto use removeItem)");
        }
        getItem(productId).setQuantity(newQty);
        this.totalAmount = calculateTotal();
    }

    public void removeItem(Long productId) {
        ensureEditable();
        items.remove(getItem(productId));
        this.totalAmount = calculateTotal();
    }

    public void clearItems() {
        ensureEditable();
        items.clear();
        this.totalAmount = BigDecimal.ZERO;
    }

    public Integer getTotalQuantity() {
        return items.stream().mapToInt(OrderItem::getQuantity).sum();
    }

    public BigDecimal calculateTotal() {
        return items.stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    // ---- Compra y estado ----

    // Confirma el pedido y lo pasa a preparación. Si ya no está pendiente no hace nada.
    public void confirmOrder() {
        if (status != OrderStatus.PENDIENTE) {
            return;
        }
        if (items.isEmpty()) {
            throw new RuntimeException("El pedido debe tener al menos un item");
        }
        if (shippingAddress == null) {
            throw new RuntimeException("La direccion de envio es obligatoria");
        }
        this.status = OrderStatus.EN_PREPARACION;
    }

    public void updateStatus(OrderStatus newStatus) {
        if (newStatus == null) {
            throw new RuntimeException("El estado es obligatorio");
        }
        this.status = newStatus;
    }

    // Devuelve true si el pedido quedó cancelado.
    public Boolean cancelOrder() {
        if (!canBeCancelled()) {
            return false;
        }
        this.status = OrderStatus.CANCELADO;
        return true;
    }

    public Boolean canBeCancelled() {
        return status != OrderStatus.ENVIADO
                && status != OrderStatus.ENTREGADO
                && status != OrderStatus.CANCELADO;
    }

    // ---- helpers ----

    private void ensureEditable() {
        if (status != OrderStatus.PENDIENTE) {
            throw new RuntimeException("Solo se puede modificar un pedido pendiente");
        }
    }

    private OrderItem getItem(Long productId) {
        return items.stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("El producto " + productId + " no esta en el pedido"));
    }
}
