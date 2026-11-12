package com.servicio.pedidos.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "codigoms-catalogo", url = "${catalog.service.url}")
public interface CatalogClient {

    @GetMapping("/api/v1/products/{id}")
    CatalogProductResponse getProduct(@PathVariable("id") Long productId);

    @PostMapping("/api/v1/products/{id}/stock")
    CatalogProductResponse adjustStock(@PathVariable("id") Long productId,
                                       @RequestBody CatalogStockAdjustment request);
}
