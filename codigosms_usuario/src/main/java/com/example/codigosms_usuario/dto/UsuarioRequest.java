package com.example.codigosms_usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioRequest {

    private String nombreUsuario;

    private String nombreReal;

    private String rut;

}
