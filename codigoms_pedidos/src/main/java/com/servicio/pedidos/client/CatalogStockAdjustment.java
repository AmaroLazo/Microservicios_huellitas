package com.servicio.pedidos.client;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CatalogStockAdjustment {
    private Integer quantity;
    private String reason;
}
