package com.servicio.codigoms_pagos.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;

import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderResponse {
    private Long orderId;
    private String status;
    private BigDecimal total;
}
