package com.example.codigosms_boleta.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CarritoDTO {
    private Long id;
    private String usuario;
    private LocalDateTime fechaCreacion;
    private List<ItemCarritoDTO> items;
    

    private BigDecimal montoTotal;
}
