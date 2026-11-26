package com.example.codigosms_usuario.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DivisionesChileTest {
    @Test
    void validaComunasConElCatalogoNacional() throws Exception {
        var divisiones = new DivisionesChile(new ObjectMapper());
        var regiones = divisiones.listarRegiones();

        assertEquals(16, regiones.size());
        assertEquals(346, regiones.stream().mapToInt(region -> region.comunas().size()).sum());
        var biobio = regiones.stream().filter(region -> region.nombre().equals("Región del Biobío"))
                .findFirst().orElseThrow();
        assertEquals(33, biobio.comunas().size());
        assertDoesNotThrow(() -> divisiones.validarComuna(biobio.nombre(), "Coronel"));
        assertThrows(IllegalArgumentException.class, () -> divisiones.validarComuna(biobio.nombre(), "Chillán"));
        assertThrows(IllegalArgumentException.class, () -> divisiones.validarComuna("Región inventada", "Coronel"));
    }
}
