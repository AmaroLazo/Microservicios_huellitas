package com.example.codigosms_usuario.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// ResetDTO del diagrama
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResetRequest {

    private String token;

    private String passwordNueva;

}
