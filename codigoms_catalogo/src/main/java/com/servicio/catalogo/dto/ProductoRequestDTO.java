package com.servicio.catalogo.dto;

import java.math.BigDecimal;

import com.servicio.catalogo.model.TipoMascota;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ProductoRequestDTO {

    @NotBlank private String codigo;
    @NotBlank private String nombre;
    private String descripcion;

    @NotNull @DecimalMin("0.01")
    private BigDecimal precio;

    @DecimalMin("0.0") @DecimalMax("100.0")
    private BigDecimal porcentajeDescuento;

    @NotNull
    private Long categoriaId;

    private TipoMascota tipoMascota;

    @NotNull @Min(0)
    private Integer stock;

    private String imagenUrl;
}