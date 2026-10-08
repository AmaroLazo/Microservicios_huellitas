package com.servicio.carrito.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.servicio.carrito.dto.UserDTO;

@FeignClient(name = "codigoms-usuarios", url = "${usuarios.service.url}")
public interface UserClient {

    @GetMapping("/api/usuarios/{id}")
    UserDTO getUserById(@PathVariable("id") Long id);
}
