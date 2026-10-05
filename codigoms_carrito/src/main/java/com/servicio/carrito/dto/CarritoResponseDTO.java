package com.servicio.carrito.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Data;

@Data
public class CarritoResponseDTO {
    private Long id;
    private String usuario;
    private LocalDateTime fechaCreacion;
    private List<ItemCarritoResponseDTO> items;
    private BigDecimal total;
}
