package com.example.codigosms_usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// PasswordDTO del diagrama
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PasswordRequest {

    private String passwordActual;

    private String passwordNueva;

}
