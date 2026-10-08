package com.servicio.carrito.dto;

import java.math.BigDecimal;

import lombok.Data;

// Campos del ProductoResponseDTO del microservicio de catalogo (solo los que usa el carrito)
@Data
public class ProductDTO {
    private Long id;
    private String nombre;
    private BigDecimal precio;
    private BigDecimal porcentajeDescuento;
    private Integer stock;
    private boolean activo;
}
