package com.example.codigosms_usuario.dto;

import java.time.LocalDateTime;

import com.example.codigosms_usuario.model.Rol;

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

    private String nombre;

    private String email;

    private Rol rol;

    private Boolean activo;

    private LocalDateTime fechaCreacion;

}
