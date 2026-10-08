package com.servicio.carrito.dto;

import java.time.LocalDateTime;

import lombok.Data;

// Mismos campos que UsuarioResponse del microservicio de usuarios (rol como String)
@Data
public class UserDTO {
    private Long usuarioId;
    private String nombre;
    private String email;
    private String rol;
    private Boolean activo;
    private LocalDateTime fechaCreacion;
}
