package com.example.codigosms_usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserRequest {

    @NotBlank
    private String name;

    @NotBlank
    @Email
    private String email;

    @Size(max = 30)
    private String phone;

    // Obligatoria al registrar; se ignora al actualizar el perfil
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;
}
