package com.servicio.carrito.dto;

import java.util.ArrayList;
import java.util.List;

import lombok.Data;

@Data
public class CartValidation {
    private boolean valid;
    private List<String> errors = new ArrayList<>();
}
