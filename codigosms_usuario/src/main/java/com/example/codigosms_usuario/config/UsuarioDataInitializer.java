package com.example.codigosms_usuario.config;

import com.example.codigosms_usuario.model.Usuario;
import com.example.codigosms_usuario.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class UsuarioDataInitializer implements CommandLineRunner {
    private final UsuarioRepository usuarioRepository;

    @Override
    public void run(String... args) {
        if (usuarioRepository.count() > 0) {
            log.info(">>> DataInitializer: usuarios ya existentes, se omite carga");
            return;
        }

        usuarioRepository.save(Usuario.builder()
                .nombreUsuario("PepitoPro")
                .nombreReal("Pedro Pérez")
                .rut("12345678-9")
                .build());

        usuarioRepository.save(Usuario.builder()
                .nombreUsuario("Panchosky")
                .nombreReal("Francisco Adrovez")
                .rut("21469836-6")
                .build());

        log.info(">>> DataInitializer: Usuarios de prueba añadidos correctamente");
    }
}
