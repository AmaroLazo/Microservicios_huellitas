package com.servicio.catalogo.dto;

import java.math.BigDecimal;

import com.servicio.catalogo.model.TipoMascota;

import lombok.Data;

@Data
public class ProductoResponseDTO {
    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Long categoriaId;
    private String categoriaNombre;
    private TipoMascota tipoMascota;
    private Integer stock;
    private boolean activo;
    // Calculado: activo && stock > 0 (HU-11 + HU-18). No se persiste.
    private boolean disponible;
    private String imagenUrl;
}