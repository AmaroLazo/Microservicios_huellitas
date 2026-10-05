package com.example.codigosms_boleta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class BoletaCompraResponse {
    private Long compraId;
    private Long carritoId;
    private String usuario;
    private BigDecimal montoTotal;
    private String estado;
    private LocalDateTime fechaCompra;
}
