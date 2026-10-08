package com.example.codigosms_usuario.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.codigosms_usuario.model.TokenRecuperacion;

// No sale en el diagrama, pero es necesario para guardar/buscar el token
public interface TokenRecuperacionRepository extends JpaRepository<TokenRecuperacion, Long> {
    Optional<TokenRecuperacion> findByToken(String token);
}
