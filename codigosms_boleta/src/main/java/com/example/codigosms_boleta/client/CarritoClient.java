package com.example.codigosms_boleta.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.example.codigosms_boleta.dto.CarritoDTO;

@FeignClient(name = "codigoms-carrito", url= "http://localhost:8083")
public interface CarritoClient {
    @GetMapping("/api/carritos/{id}")
    CarritoDTO obtenerCarritoPorId(@PathVariable("id") Long id);    
}
