package com.servicio.carrito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.servicio.carrito.dto.LibroDTO;



@FeignClient(name = "codigoms-catalogo", url = "${catalogo.service.url}")
public interface CatalogoClient {

    @GetMapping("/api/libros/{id}")
    LibroDTO obtenerLibro(@PathVariable Long id);
}
