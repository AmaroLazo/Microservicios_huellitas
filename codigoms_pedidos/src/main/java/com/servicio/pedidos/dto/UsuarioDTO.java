package com.servicio.pedidos.dto;
import java.time.LocalDate;

import lombok.Data;

@Data
public class UsuarioDTO {
    private Long id;
    private String nombreUsuario;
    private String nombreReal;
    private String rut;
    private LocalDate fechaCreacion;
}
