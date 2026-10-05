package com.servicio.catalogo.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class LibroRequestDTO {

    @NotBlank private String titulo;
    @NotBlank private String autor;
    @NotBlank private String isbn;
    @NotNull @DecimalMin("0.01") private BigDecimal precio;
    @NotBlank private String categoriaNombre;}
