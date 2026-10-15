package com.example.codigosms_usuario.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.codigosms_usuario.dto.AddressRequest;
import com.example.codigosms_usuario.dto.AddressResponse;
import com.example.codigosms_usuario.dto.AuthResponse;
import com.example.codigosms_usuario.dto.LoginRequest;
import com.example.codigosms_usuario.dto.PasswordRequest;
import com.example.codigosms_usuario.dto.ResetRequest;
import com.example.codigosms_usuario.dto.UserRequest;
import com.example.codigosms_usuario.dto.UserResponse;
import com.example.codigosms_usuario.model.Role;
import com.example.codigosms_usuario.model.User;
import com.example.codigosms_usuario.service.UserService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .userId(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .role(u.getRole())
                .active(u.getActive())
                .createdAt(u.getCreatedAt())
                .addresses(u.getAddresses().stream()
                        .map(a -> AddressResponse.builder()
                                .id(a.getId())
                                .street(a.getStreet())
                                .city(a.getCity())
                                .build())
                        .toList())
                .build();
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody UserRequest dto) {
        return ResponseEntity.status(201).body(toResponse(service.register(dto)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest dto) {
        return ResponseEntity.ok(service.login(dto.getEmail(), dto.getPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Sin sesiones ni tokens JWT no hay nada que invalidar en el servidor
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getProfile(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.getProfile(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UserResponse> updateProfile(@PathVariable Long id,
                                                      @Valid @RequestBody UserRequest dto) {
        return ResponseEntity.ok(toResponse(service.updateProfile(id, dto)));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(@PathVariable Long id,
                                               @RequestBody PasswordRequest dto) {
        service.changePassword(id, dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> resetPassword(@RequestParam String email) {
        service.resetPassword(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@RequestBody ResetRequest dto) {
        service.confirmPasswordReset(dto.getToken(), dto.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateAccount(@PathVariable Long id) {
        service.deactivateAccount(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/addresses")
    public ResponseEntity<UserResponse> addAddress(@PathVariable Long id,
                                                   @Valid @RequestBody AddressRequest dto) {
        return ResponseEntity.status(201).body(toResponse(service.addAddress(id, dto)));
    }

    @DeleteMapping("/{id}/addresses/{addressId}")
    public ResponseEntity<UserResponse> removeAddress(@PathVariable Long id,
                                                      @PathVariable Long addressId) {
        return ResponseEntity.ok(toResponse(service.removeAddress(id, addressId)));
    }

    // ---- Administración ----

    @GetMapping
    public ResponseEntity<List<UserResponse>> listUsers() {
        return ResponseEntity.ok(service.listUsers().stream().map(this::toResponse).toList());
    }

    @GetMapping("/search")
    public ResponseEntity<List<UserResponse>> searchUsers(@RequestParam String query) {
        return ResponseEntity.ok(service.searchUsers(query).stream().map(this::toResponse).toList());
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<Void> toggleUserStatus(@PathVariable Long id) {
        service.toggleUserStatus(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/role")
    public ResponseEntity<Void> updateUserRole(@PathVariable Long id, @RequestParam Role role) {
        service.updateUserRole(id, role);
        return ResponseEntity.noContent().build();
    }
}
