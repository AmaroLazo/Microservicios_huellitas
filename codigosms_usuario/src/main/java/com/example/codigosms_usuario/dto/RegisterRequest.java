package com.example.codigosms_usuario.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class RegisterRequest {
    @NotBlank(message = "El nombre completo es obligatorio")
    @Size(max = 100)
    private String name;

    @NotBlank(message = "El correo es obligatorio")
    @Email
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
    private String password;

    @NotBlank(message = "El teléfono es obligatorio")
    @Size(min = 8, max = 30)
    private String phone;

    @NotBlank(message = "La calle y número son obligatorios")
    @Size(max = 100)
    private String street;

    @NotBlank(message = "La ciudad o comuna es obligatoria")
    @Size(max = 50)
    private String city;
}
