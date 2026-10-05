package com.servicio.carrito.client;


import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import com.servicio.carrito.dto.UsuarioDTO;

@FeignClient(name = "codigoms-usuarios", url = "${usuarios.service.url}")
public interface UsuariosClient {

    @GetMapping("/api/usuarios/usuario/{nombreUsuario}")
    UsuarioDTO buscarPorNombreUsuario(@PathVariable String nombreUsuario);
}
