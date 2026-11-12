package com.servicio.product.model;

import java.math.BigDecimal;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false, length = 150)
    private String name;

    @Column(length = 500)
    private String description;

    private String imageUrl;

    @NotNull
    @DecimalMin("0.01")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @NotNull
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    @Column(nullable = false, precision = 5, scale = 2)
    private BigDecimal discountPercent = BigDecimal.ZERO;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer stockQuantity = 0;

    @NotNull
    @Min(0)
    @Column(nullable = false)
    private Integer minStock = 0;

    @Column(nullable = false)
    private Boolean active = true;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Version
    private Long version;
}
