package com.servicio.catalogo.dto;

import java.math.BigDecimal;

import lombok.Data;



@Data

public class LibroResponseDTO {
    private Long id;
    private String titulo;
    private String autor;
    private String isbn;
    private BigDecimal precio;
    private String categoriaNombre;
    private boolean disponible;
}
