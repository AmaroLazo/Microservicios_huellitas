package com.example.codigosms_usuario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.codigosms_usuario.model.PasswordResetToken;

// No sale en el diagrama, pero es necesario para guardar/buscar el token
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {
    Optional<PasswordResetToken> findByToken(String token);
}
