package com.example.codigosms_usuario.service;

import java.io.IOException;
import java.util.List;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class DivisionesChile {
    private final List<Region> regiones;

    public DivisionesChile(ObjectMapper mapper) throws IOException {
        // Fuente: https://www.bcn.cl/siit/mapoteca/comunas
        try (var datos = new ClassPathResource("regiones-chile.json").getInputStream()) {
            regiones = mapper.readValue(datos, new TypeReference<List<Region>>() {}).stream()
                    .map(region -> new Region(region.nombre(), List.copyOf(region.comunas())))
                    .toList();
        }
    }

    public List<Region> listarRegiones() {
        return regiones;
    }

    public void validarComuna(String region, String comuna) {
        Region seleccionada = regiones.stream().filter(item -> item.nombre().equals(region)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Selecciona una región válida de Chile."));
        if (!seleccionada.comunas().contains(comuna)) {
            throw new IllegalArgumentException("La comuna no pertenece a la región seleccionada.");
        }
    }

    public record Region(String nombre, List<String> comunas) {}
}
