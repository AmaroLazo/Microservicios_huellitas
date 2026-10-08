package com.example.codigosms_usuario.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.codigosms_usuario.dto.AuthResponse;
import com.example.codigosms_usuario.dto.UsuarioRequest;
import com.example.codigosms_usuario.model.TokenRecuperacion;
import com.example.codigosms_usuario.model.Usuario;
import com.example.codigosms_usuario.repository.TokenRecuperacionRepository;
import com.example.codigosms_usuario.repository.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final TokenRecuperacionRepository tokenRepository;

    // Hash simple con SHA-256 (para algo real conviene BCrypt)
    private String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo encriptar la contraseña", e);
        }
    }

    private Usuario buscarPorId(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Usuario con ID " + id + " no fue encontrado"
                ));
    }

    @Transactional
    public Usuario crearUsuario(UsuarioRequest dto) {
        log.info("Creando Usuario con email={}", dto.getEmail());

        if (dto.getPassword() == null || dto.getPassword().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        Usuario u = new Usuario();
        u.setNombre(dto.getNombre());
        u.setEmail(dto.getEmail());
        u.setPasswordHash(hashPassword(dto.getPassword()));

        return usuarioRepository.save(u);
    }

    public AuthResponse autenticar(String email, String password) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!u.getActivo() || !u.getPasswordHash().equals(hashPassword(password))) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        log.info("Login exitoso para usuario id={}", u.getId());
        return AuthResponse.builder()
                .usuarioId(u.getId())
                .email(u.getEmail())
                .rol(u.getRol())
                .mensaje("Login exitoso")
                .build();
    }

    public Usuario obtenerDetalleUsuario(Long id) {
        return buscarPorId(id);
    }

    @Transactional
    public Usuario actualizarUsuario(Long id, UsuarioRequest dto) {
        Usuario u = buscarPorId(id);

        if (!u.getEmail().equals(dto.getEmail()) && usuarioRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        u.setNombre(dto.getNombre());
        u.setEmail(dto.getEmail());
        return usuarioRepository.save(u);
    }

    @Transactional
    public void cambiarPassword(Long id, String passwordActual, String passwordNueva) {
        Usuario u = buscarPorId(id);

        if (!u.getPasswordHash().equals(hashPassword(passwordActual))) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }

        u.setPasswordHash(hashPassword(passwordNueva));
        usuarioRepository.save(u);
        log.info("Contraseña cambiada para usuario id={}", id);
    }

    @Transactional
    public void crearTokenReset(String email) {
        Usuario u = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con ese email"));

        TokenRecuperacion t = new TokenRecuperacion();
        t.setUsuarioId(u.getId());
        t.setToken(UUID.randomUUID().toString());
        t.setExpiraEn(LocalDateTime.now().plusMinutes(30));
        t.setUsado(false);
        tokenRepository.save(t);

        // Aquí iría el envío por correo; por ahora solo se registra en el log
        log.info("Token de recuperación generado para usuario id={}: {}", u.getId(), t.getToken());
    }

    @Transactional
    public void resetPassword(String token, String passwordNueva) {
        TokenRecuperacion t = tokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Token no encontrado"));

        if (t.getUsado() || t.estaExpirado()) {
            throw new IllegalArgumentException("El token es inválido o ya expiró");
        }

        Usuario u = buscarPorId(t.getUsuarioId());
        u.setPasswordHash(hashPassword(passwordNueva));
        usuarioRepository.save(u);

        t.setUsado(true);
        tokenRepository.save(t);
        log.info("Contraseña restablecida para usuario id={}", u.getId());
    }

    @Transactional
    public void setActivo(Long id, Boolean activo) {
        Usuario u = buscarPorId(id);
        u.setActivo(activo);
        usuarioRepository.save(u);
        log.info("Usuario id={} activo={}", id, activo);
    }

    public List<Usuario> listarUsuarios() {
        return usuarioRepository.findAll();
    }

}
