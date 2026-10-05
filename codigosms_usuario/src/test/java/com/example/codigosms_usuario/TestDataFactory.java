package com.example.codigosms_usuario;

import java.time.LocalDateTime;

import com.example.codigosms_usuario.dto.UsuarioRequest;
import com.example.codigosms_usuario.model.Usuario;

import net.datafaker.Faker;

public class TestDataFactory {

    private static final Faker faker = new Faker();

    public static UsuarioRequest unUsuarioRequest(String nombreUsuario, String nombreReal, String rut) {
        return new UsuarioRequest(nombreUsuario, nombreReal, rut);
    }

    public static Usuario unUsuario(String nombreUsuario, String nombreReal, String rut) {
        return Usuario.builder()
                .id(faker.number().randomNumber())
                .nombreUsuario(nombreUsuario)
                .nombreReal(nombreReal)
                .rut(rut)
                .fechaCreacion(LocalDateTime.now())
                .build();
    }
}