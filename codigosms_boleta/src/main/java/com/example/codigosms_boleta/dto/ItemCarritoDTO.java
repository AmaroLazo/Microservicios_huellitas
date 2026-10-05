package com.example.codigosms_boleta.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ItemCarritoDTO {
    private Long id;
    private Long libroId;       
    private String tituloLibro;  
    private BigDecimal precioUnitario; 
    private Integer cantidad;    
}
