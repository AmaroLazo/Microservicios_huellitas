package com.example.codigosms_usuario.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.http.CacheControl;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestAttribute;
import org.springframework.web.bind.annotation.RestController;

import com.example.codigosms_usuario.dto.AddressRequest;
import com.example.codigosms_usuario.dto.AddressResponse;
import com.example.codigosms_usuario.dto.AuthResponse;
import com.example.codigosms_usuario.dto.LoginRequest;
import com.example.codigosms_usuario.dto.PasswordRequest;
import com.example.codigosms_usuario.dto.ResetRequest;
import com.example.codigosms_usuario.dto.UserRequest;
import com.example.codigosms_usuario.dto.RegisterRequest;
import com.example.codigosms_usuario.dto.UserResponse;
import com.example.codigosms_usuario.model.Role;
import com.example.codigosms_usuario.model.User;
import com.example.codigosms_usuario.service.UserService;
import com.example.codigosms_usuario.service.DivisionesChile;
import com.example.codigosms_usuario.config.JwtService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService service;
    private final JwtService jwt;
    private final DivisionesChile divisionesChile;

    private UserResponse toResponse(User u) {
        return UserResponse.builder()
                .userId(u.getId())
                .name(u.getName())
                .email(u.getEmail())
                .phone(u.getPhone())
                .role(u.getRole())
                .active(u.getActive())
                .createdAt(u.getCreatedAt())
                .addresses(u.getAddresses().stream()
                        .map(a -> AddressResponse.builder()
                                .id(a.getId())
                                .street(a.getStreet())
                                .city(a.getCity())
                                .houseNumber(a.getHouseNumber())
                                .region(a.getRegion())
                                .postalCode(a.getPostalCode())
                                .build())
                        .toList())
                .build();
    }

    @PostMapping
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest dto) {
        return ResponseEntity.status(201).body(toResponse(service.register(dto)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest dto) {
        AuthResponse session = service.login(dto.getEmail(), dto.getPassword());
        session.setAccessToken(jwt.issue(session.getUserId(), session.getRole()));
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(session);
    }

    @GetMapping("/regiones")
    public List<DivisionesChile.Region> regiones() {
        return divisionesChile.listarRegiones();
    }

    @GetMapping("/session")
    public ResponseEntity<JwtService.Identity> session(@RequestAttribute("huellitasIdentity") JwtService.Identity identity) {
        return ResponseEntity.ok().cacheControl(CacheControl.noStore()).body(identity);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // El cliente elimina el token al cerrar sesión.
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
                                               @Valid @RequestBody PasswordRequest dto) {
        service.changePassword(id, dto.getCurrentPassword(), dto.getNewPassword());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/request")
    public ResponseEntity<Void> resetPassword(@RequestParam String email) {
        service.resetPassword(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/confirm")
    public ResponseEntity<Void> confirmPasswordReset(@Valid @RequestBody ResetRequest dto) {
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
