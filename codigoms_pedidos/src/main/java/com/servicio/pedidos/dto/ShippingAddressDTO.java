package com.servicio.pedidos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ShippingAddressDTO {

    @NotBlank(message = "La calle es obligatoria")
    private String street;

    @NotBlank(message = "La ciudad es obligatoria")
    private String city;

    @Size(max = 20)
    private String houseNumber;

    @Size(max = 100)
    private String region;

    @Pattern(regexp = "[0-9]{7}", message = "El código postal debe tener 7 dígitos")
    private String postalCode;
}
