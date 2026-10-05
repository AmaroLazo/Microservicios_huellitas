package com.example.codigosms_boleta.dto;

import com.example.codigosms_boleta.model.MetodoPago;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder


public class BoletaCompraRequest {
    @NotNull(message = "El ID del carrito es obligatorio")
    private Long carritoId;

    @NotBlank(message = "El nombre de usuario es obligatorio")
    private String usuario;

    @NotNull(message= "El metodo de pago es obligatorio")
    private MetodoPago metodoPago;

}
