package com.servicio.carrito.client;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.servicio.carrito.dto.ProductDTO;

import feign.FeignException;

@FeignClient(name = "codigoms-catalogo", url = "${catalogo.service.url}")
public interface CatalogClient {

    @GetMapping("/api/v1/productos/{id}")
    ProductDTO getProduct(@PathVariable("id") Long productId);

    default Boolean checkStock(Long productId, int quantity) {
        try {
            ProductDTO p = getProduct(productId);
            return p.isActivo() && p.getStock() != null && p.getStock() >= quantity;
        } catch (FeignException.NotFound e) {
            return false;
        }
    }

    // Precio actual con el descuento del catalogo aplicado
    default BigDecimal getCurrentPrice(Long productId) {
        ProductDTO p = getProduct(productId);
        BigDecimal discount = p.getPorcentajeDescuento() != null ? p.getPorcentajeDescuento() : BigDecimal.ZERO;
        BigDecimal factor = BigDecimal.valueOf(100).subtract(discount).divide(BigDecimal.valueOf(100));
        return p.getPrecio().multiply(factor).setScale(2, RoundingMode.HALF_UP);
    }
}
