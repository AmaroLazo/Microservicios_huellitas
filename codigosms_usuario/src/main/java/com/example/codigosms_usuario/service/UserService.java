package com.example.codigosms_usuario.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.codigosms_usuario.dto.AddressRequest;
import com.example.codigosms_usuario.dto.AdminUserRequest;
import com.example.codigosms_usuario.dto.AuthResponse;
import com.example.codigosms_usuario.dto.UserRequest;
import com.example.codigosms_usuario.dto.RegisterRequest;
import com.example.codigosms_usuario.model.Address;
import com.example.codigosms_usuario.model.PasswordResetToken;
import com.example.codigosms_usuario.model.Role;
import com.example.codigosms_usuario.model.User;
import com.example.codigosms_usuario.repository.PasswordResetTokenRepository;
import com.example.codigosms_usuario.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final DivisionesChile divisionesChile;

    private Address toAddress(AddressRequest dto) {
        divisionesChile.validarComuna(dto.getRegion().trim(), dto.getCity().trim());
        return Address.builder()
                .street(dto.getStreet().trim())
                .city(dto.getCity().trim())
                .houseNumber(dto.getHouseNumber().trim())
                .region(dto.getRegion().trim())
                .postalCode(dto.getPostalCode())
                .build();
    }

    private User findUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Usuario con ID " + id + " no fue encontrado"));
    }

    /** Registro de pruebas: guarda la contraseña literalmente (NO utilizar en producción). */
    @Transactional
    public User register(RegisterRequest dto) {
        validatePassword(dto.getPassword());
        validateRegistrationAddress(dto);
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(email);
        user.setPhone(dto.getPhone());
        user.setPasswordHash(dto.getPassword());
        user.setRole(Role.CUSTOMER);
        user.setActive(true);
        List<Address> addresses = new ArrayList<>();
        addresses.add(toAddress(dto));
        user.setAddresses(addresses);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(String email, String password) {
        User user = userRepository.findByEmail(email == null ? "" : email.trim().toLowerCase())
                .orElseThrow(() -> new IllegalArgumentException("Credenciales inválidas"));
        if (!Boolean.TRUE.equals(user.getActive()) || password == null
                || !password.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("Credenciales inválidas");
        }
        log.info("Login exitoso para usuario id={}", user.getId());
        return AuthResponse.builder().userId(user.getId()).email(user.getEmail())
                .role(user.getRole()).message("Login exitoso").build();
    }

    @Transactional(readOnly = true)
    public User getProfile(Long id) {
        return findUser(id);
    }

    @Transactional
    public User updateProfile(Long id, UserRequest dto) {
        User user = findUser(id);
        String email = dto.getEmail().trim().toLowerCase();
        if (!user.getEmail().equalsIgnoreCase(email) && userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("El email ya está registrado");
        }
        user.setName(dto.getName().trim());
        user.setEmail(email);
        // Al editar el perfil desde el administrador, también se puede cambiar el teléfono.
        if (dto.getPhone() != null) user.setPhone(dto.getPhone().trim());
        return userRepository.save(user);
    }

    @Transactional
    public User updateFromAdmin(Long id, long adminId, AdminUserRequest dto) {
        if (id == adminId && (dto.getRole() != Role.ADMIN || !Boolean.TRUE.equals(dto.getActive()))) {
            throw new IllegalArgumentException("No puedes quitarte el rol ADMIN ni desactivar tu cuenta.");
        }
        User user = updateProfile(id, dto);
        user.setRole(dto.getRole());
        user.setActive(dto.getActive());
        return userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = findUser(id);
        if (currentPassword == null || !currentPassword.equals(user.getPasswordHash())) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        validatePassword(newPassword);
        user.setPasswordHash(newPassword);
        userRepository.save(user);
        log.info("Contraseña cambiada para usuario id={}", id);
    }

    /** Genera un token de recuperación válido durante 30 minutos. */
    @Transactional
    public void resetPassword(String email) {
        User user = userRepository.findByEmail(email == null ? "" : email.trim().toLowerCase())
                .orElseThrow(() -> new EntityNotFoundException("No existe un usuario con ese email"));
        PasswordResetToken token = PasswordResetToken.builder()
                .userId(user.getId())
                .token(UUID.randomUUID().toString())
                .expiresAt(LocalDateTime.now().plusMinutes(30))
                .used(false)
                .build();
        tokenRepository.save(token);
        // Para este proyecto de ejemplo, el token queda disponible en el log en lugar de enviarse por correo.
        log.info("Token de recuperación generado para usuario id={}: {}", user.getId(), token.getToken());
    }

    /** Valida el token y permite usarlo una sola vez para cambiar la contraseña. */
    @Transactional
    public void confirmPasswordReset(String tokenValue, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new EntityNotFoundException("Token no encontrado"));
        if (Boolean.TRUE.equals(token.getUsed()) || token.getExpiresAt() == null
                || LocalDateTime.now().isAfter(token.getExpiresAt())) {
            throw new IllegalArgumentException("El token es inválido, ya se usó o expiró");
        }
        validatePassword(newPassword);
        User user = findUser(token.getUserId());
        user.setPasswordHash(newPassword);
        userRepository.save(user);
        token.setUsed(true);
        tokenRepository.save(token);
        log.info("Contraseña restablecida para usuario id={}", user.getId());
    }

    @Transactional
    public void deactivateAccount(Long id) {
        User user = findUser(id);
        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void toggleUserStatus(Long userId) {
        User user = findUser(userId);
        user.setActive(!Boolean.TRUE.equals(user.getActive()));
        userRepository.save(user);
    }

    @Transactional
    public void updateUserRole(Long userId, Role newRole) {
        if (newRole == null) throw new IllegalArgumentException("El rol es obligatorio");
        User user = findUser(userId);
        user.setRole(newRole);
        userRepository.save(user);
    }

    @Transactional
    public User addAddress(Long userId, AddressRequest dto) {
        User user = findUser(userId);
        Address address = toAddress(dto);
        user.getAddresses().add(address);
        return userRepository.save(user);
    }

    @Transactional
    public User removeAddress(Long userId, Long addressId) {
        User user = findUser(userId);
        boolean removed = user.getAddresses().removeIf(address -> address.getId() != null && address.getId().equals(addressId));
        if (!removed) throw new EntityNotFoundException("La dirección " + addressId + " no pertenece al usuario");
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<User> searchUsers(String query) {
        if (query == null || query.isBlank()) return userRepository.findAll();
        String value = query.trim();
        return userRepository.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(value, value);
    }

    private void validateRegistrationAddress(RegisterRequest dto) {
        if (dto.getStreet().trim().length() < 3) {
            throw new IllegalArgumentException("La calle debe tener al menos 3 caracteres.");
        }
        if (!dto.getHouseNumber().trim().matches("[0-9]+[A-Za-z]?")) {
            throw new IllegalArgumentException("Ingresa el número de casa/departamento, con una letra opcional al final (ej.: 123 o 123B).");
        }
    }

    private void validatePassword(String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (password.length() < 8) {
            throw new IllegalArgumentException("La contraseña debe tener al menos 8 caracteres");
        }
    }
}
