package com.servicio.pedidos.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class CatalogProductResponse {
    private Long id;
    private String name;
    private BigDecimal finalPrice;
    private Integer stockQuantity;
    private Boolean active;
    private Boolean available;
}
