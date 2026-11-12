package com.example.codigosms_usuario.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "La calle es obligatoria")
    private String street;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;
}
