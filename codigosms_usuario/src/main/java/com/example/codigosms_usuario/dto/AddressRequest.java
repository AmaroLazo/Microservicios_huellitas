package com.example.codigosms_usuario.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddressRequest {

    @NotBlank(message = "La calle es obligatoria")
    @Size(max = 100)
    private String street;

    @NotBlank(message = "La comuna es obligatoria")
    @Size(max = 50)
    private String city;

    @NotBlank(message = "El número de casa/departamento es obligatorio")
    @Size(max = 20)
    private String houseNumber;

    @NotBlank(message = "La región es obligatoria")
    @Size(max = 100)
    private String region;

    @NotBlank(message = "El código postal es obligatorio")
    @Pattern(regexp = "[0-9]{7}", message = "El código postal debe tener 7 dígitos")
    private String postalCode;
}
