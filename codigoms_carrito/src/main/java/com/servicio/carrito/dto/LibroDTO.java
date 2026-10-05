package com.servicio.carrito.dto;

import java.math.BigDecimal;

import lombok.Data;


@Data
public class LibroDTO {
    private Long id;
    private String titulo;
    private String isbn;
    private BigDecimal precio;
    private String categoriaNombre;
}
