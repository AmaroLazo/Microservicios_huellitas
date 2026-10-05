package com.example.codigosms_usuario.dto;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UsuarioResponse {

    private Long usuarioId;

    private String nombreUsuario;

    private String nombreReal;
    
    private String rut;

    private LocalDateTime fechaCreacion;

}
