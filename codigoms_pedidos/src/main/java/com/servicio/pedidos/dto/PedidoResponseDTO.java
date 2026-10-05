package com.servicio.pedidos.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Data;

@Data
public class PedidoResponseDTO {
    private Long id;
    private Long libroId;
    private String tituloLibro;
    private BigDecimal precioUnitario;
    private String cliente;
    private Integer cantidad;
    private BigDecimal total;
    private LocalDateTime fechaPedido;
}
