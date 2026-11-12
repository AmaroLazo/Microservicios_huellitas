package com.servicio.product.model;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.ForeignKey;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotNull;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.EqualsAndHashCode;

@Data
@NoArgsConstructor
@Entity
@Table(name = "stock_movements")
public class StockMovement {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Column(name = "product_id", nullable = false)
    private Long productId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
        name = "product_id",
        nullable = false,
        insertable = false,
        updatable = false,
        foreignKey = @ForeignKey(
            name = "fk_stock_movements_product"
        )
    )
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Product product;

    @NotNull
    @Column(nullable = false)
    private Integer quantity;

    @Column(length = 255)
    private String reason;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public StockMovement(
            Long id,
            Long productId,
            Integer quantity,
            String reason,
            LocalDateTime createdAt) {
        this.id = id;
        this.productId = productId;
        this.quantity = quantity;
        this.reason = reason;
        this.createdAt = createdAt;
    }
}