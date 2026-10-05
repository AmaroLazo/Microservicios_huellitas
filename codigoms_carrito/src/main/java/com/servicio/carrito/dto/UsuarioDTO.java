package com.servicio.carrito.dto;

import java.time.LocalDateTime;

import lombok.Data;
@Data
public class UsuarioDTO {
    private Long usuarioId;

    private String nombreUsuario;

    private String nombreReal;
    
    private String rut;

    private LocalDateTime fechaCreacion;
}
