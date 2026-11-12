package com.example.codigosms_usuario.config;

import java.util.List;
import java.util.Locale;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.codigosms_usuario.model.Address;
import com.example.codigosms_usuario.model.Role;
import com.example.codigosms_usuario.model.User;
import com.example.codigosms_usuario.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/** Solo datos ficticios: habilitar con app.seed.demo.enabled=true. */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "app.seed.demo.enabled", havingValue = "true")
public class DemoUsersDataInitializer implements ApplicationRunner {

    private static final int YEAR = 2026;
    private static final String DOMAIN = "@huellitas.test";
    private final UserRepository userRepository;

    private record DemoUser(String first, String last, String street, String city) {
        String email() {
            return (first.substring(0, 2) + last).toLowerCase(Locale.ROOT) + DOMAIN;
        }

        String password() {
            return first.substring(0, 2) + last.substring(0, 2) + YEAR;
        }

        String fullName() { return first + " " + last; }
    }

    private static final List<DemoUser> DEMOS = List.of(
            new DemoUser("Brandon", "Cantero", "Los Arrayanes 123", "Coronel"),
            new DemoUser("Francisco", "Adrovez", "Los Copihues 456", "Concepción"),
            new DemoUser("Valentina", "Rojas", "Las Acacias 789", "San Pedro de la Paz"),
            new DemoUser("Matías", "Herrera", "Los Alerces 321", "Talcahuano"),
            new DemoUser("Camila", "Fuentes", "Los Robles 654", "Chiguayante")
    );

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        int inserted = 0;
        for (DemoUser demo : DEMOS) {
            if (userRepository.existsByEmail(demo.email())) {
                log.info("Usuario de prueba existente: {}", demo.email());
                continue;
            }

            User user = new User();
            user.setName(demo.fullName());
            user.setEmail(demo.email());
            // IMPORTANTE: texto plano SOLO para la variante local sin BCrypt solicitada.
            // Si el login usa PasswordEncoder.matches(), sustituir por passwordEncoder.encode(...).
            user.setPasswordHash(demo.password());
            user.setRole(Role.CUSTOMER);
            user.setActive(true);
            user.setAddresses(List.of(Address.builder()
                    .street(demo.street()).city(demo.city()).build()));
            userRepository.save(user);
            inserted++;
        }
        log.info("Usuarios ficticios creados: {} de {}. Comprueba usuarios_db.users y addresses.", inserted, DEMOS.size());
    }
}
