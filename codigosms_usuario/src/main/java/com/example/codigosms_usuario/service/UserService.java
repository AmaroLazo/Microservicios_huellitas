package com.example.codigosms_usuario.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.example.codigosms_usuario.dto.AddressRequest;
import com.example.codigosms_usuario.dto.AuthResponse;
import com.example.codigosms_usuario.dto.UserRequest;
import com.example.codigosms_usuario.model.Address;
import com.example.codigosms_usuario.model.PasswordResetToken;
import com.example.codigosms_usuario.model.Role;
import com.example.codigosms_usuario.model.User;
import com.example.codigosms_usuario.repository.PasswordResetTokenRepository;
import com.example.codigosms_usuario.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                    "Usuario con ID " + id + " no fue encontrado"
                ));
    }

    @Transactional
    public User register(UserRequest dto) {
        log.info("Registrando usuario con email={}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        return userRepository.save(User.register(dto.getName(), dto.getEmail(), dto.getPassword()));
    }

    public AuthResponse login(String email, String password) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));

        if (!u.login(email, password)) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }

        log.info("Login exitoso para usuario id={}", u.getId());
        return AuthResponse.builder()
                .userId(u.getId())
                .email(u.getEmail())
                .role(u.getRole())
                .message("Login exitoso")
                .build();
    }

    public User getProfile(Long id) {
        return findUser(id);
    }

    @Transactional
    public User updateProfile(Long id, UserRequest dto) {
        User u = findUser(id);

        if (!u.getEmail().equals(dto.getEmail()) && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("El email ya está registrado");
        }

        u.updateProfile(dto.getName(), dto.getEmail());
        return userRepository.save(u);
    }

    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User u = findUser(id);
        u.changePassword(currentPassword, newPassword);
        userRepository.save(u);
        log.info("Contraseña cambiada para usuario id={}", id);
    }

    // Paso 1 de la recuperación: genera el token de recuperación para el email indicado
    @Transactional
    public void resetPassword(String email) {
        User u = userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con ese email"));

        PasswordResetToken t = PasswordResetToken.builder()
                .userId(u.getId())
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        tokenRepository.save(t);

        // Aquí iría el envío por correo; por ahora solo se registra en el log
        log.info("Token de recuperación generado para usuario id={}: {}", u.getId(), t.getToken());
    }

    // Paso 2 de la recuperación: usa el token para fijar la contraseña nueva
    @Transactional
    public void confirmPasswordReset(String token, String newPassword) {
        PasswordResetToken t = tokenRepository.findByToken(token)
                .orElseThrow(() -> new EntityNotFoundException("Token no encontrado"));

        if (t.getUsed() || t.isExpired()) {
            throw new IllegalArgumentException("El token es inválido o ya expiró");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("La contraseña nueva es obligatoria");
        }

        User u = findUser(t.getUserId());
        u.setPasswordHash(User.hashPassword(newPassword));
        userRepository.save(u);

        t.setUsed(true);
        tokenRepository.save(t);
        log.info("Contraseña restablecida para usuario id={}", u.getId());
    }

    @Transactional
    public void deactivateAccount(Long id) {
        User u = findUser(id);
        u.deactivateAccount();
        userRepository.save(u);
        log.info("Cuenta desactivada para usuario id={}", id);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User u = findUser(userId);
        u.setActive(!u.getActive());
        userRepository.save(u);
        log.info("Usuario id={} active={}", userId, u.getActive());
    }

    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        User u = findUser(userId);
        u.setRole(newRole);
        userRepository.save(u);
        log.info("Usuario id={} ahora tiene rol {}", userId, newRole);
    }

    @Transactional
    public User addAddress(Long userId, AddressRequest dto) {
        User u = findUser(userId);
        u.addAddress(Address.builder().street(dto.getStreet()).city(dto.getCity()).build());
        return userRepository.save(u);
    }

    @Transactional
    public User removeAddress(Long userId, Long addressId) {
        User u = findUser(userId);
        u.removeAddress(addressId);
        return userRepository.save(u);
    }

    public List<User> listUsers() {
        return userRepository.findAll();
    }

    public List<User> searchUsers(String query) {
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(query, query);
    }
}
