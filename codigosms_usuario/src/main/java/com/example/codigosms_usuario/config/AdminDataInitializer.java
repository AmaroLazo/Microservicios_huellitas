package com.example.codigosms_usuario.config;

import java.util.Locale;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.codigosms_usuario.model.Role;
import com.example.codigosms_usuario.model.User;
import com.example.codigosms_usuario.repository.UserRepository;

@Component
public class AdminDataInitializer implements ApplicationRunner {
    private final UserRepository users;
    private final String email;
    private final String password;

    public AdminDataInitializer(UserRepository users,
            @Value("${HUELLITAS_ADMIN_EMAIL:admin@huellitas.test}") String email,
            @Value("${HUELLITAS_ADMIN_PASSWORD:}") String password) {
        this.users = users;
        this.email = email.trim().toLowerCase(Locale.ROOT);
        this.password = password;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.existsByRole(Role.ADMIN)) return;

        if (email.length() > 100 || !email.matches("[^\\s@]+@[^\\s@]+\\.[^\\s@]+")) {
            throw new IllegalStateException("Configura un correo válido en HUELLITAS_ADMIN_EMAIL.");
        }
        if (password.isBlank() || password.length() < 8) {
            throw new IllegalStateException("Configura HUELLITAS_ADMIN_PASSWORD con al menos 8 caracteres.");
        }
        if (Boolean.TRUE.equals(users.existsByEmail(email))) {
            throw new IllegalStateException("HUELLITAS_ADMIN_EMAIL ya pertenece a una cuenta. Usa otro correo.");
        }

        users.save(User.builder()
                .name("Administrador Huellitas")
                .email(email)
                .passwordHash(password)
                .role(Role.ADMIN)
                .active(true)
                .build());
    }
}
