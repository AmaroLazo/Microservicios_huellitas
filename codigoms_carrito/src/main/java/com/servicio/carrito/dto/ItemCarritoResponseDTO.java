package com.servicio.carrito.dto;

import java.math.BigDecimal;

import lombok.Data;

@Data
public class ItemCarritoResponseDTO {
    private Long id;
    private Long libroId;
    private String tituloLibro;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;
}
