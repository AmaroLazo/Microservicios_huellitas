package com.example.codigosms_usuario.dto;

import com.example.codigosms_usuario.model.Rol;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long usuarioId;

    private String email;

    private Rol rol;

    private String mensaje;

}
