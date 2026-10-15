package com.example.codigosms_usuario.model;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    @Column(nullable = false)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Role role = Role.CUSTOMER;

    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    // User 1 --- 0..* Address
    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id")
    @Builder.Default
    private List<Address> addresses = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    // ---- Comportamiento del diagrama ----

    public static User register(String name, String email, String password) {
        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPasswordHash(hashPassword(password));
        return user;
    }

    // true si la cuenta está activa y las credenciales coinciden
    public Boolean login(String email, String password) {
        return password != null
                && Boolean.TRUE.equals(active)
                && this.email.equalsIgnoreCase(email)
                && passwordHash.equals(hashPassword(password));
    }

    public void updateProfile(String name, String email) {
        this.name = name;
        this.email = email;
    }

    public void changePassword(String oldPassword, String newPassword) {
        if (oldPassword == null || !passwordHash.equals(hashPassword(oldPassword))) {
            throw new IllegalArgumentException("La contraseña actual es incorrecta");
        }
        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("La contraseña nueva es obligatoria");
        }
        this.passwordHash = hashPassword(newPassword);
    }

    public void deactivateAccount() {
        this.active = false;
    }

    public void addAddress(Address address) {
        if (address == null) {
            throw new IllegalArgumentException("La dirección es obligatoria");
        }
        this.addresses.add(address);
    }

    public void removeAddress(Long addressId) {
        boolean removed = this.addresses.removeIf(a -> a.getId() != null && a.getId().equals(addressId));
        if (!removed) {
            throw new IllegalArgumentException("La dirección " + addressId + " no pertenece al usuario");
        }
    }

    // Hash simple con SHA-256 (para algo real conviene BCrypt)
    public static String hashPassword(String password) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            throw new IllegalStateException("No se pudo encriptar la contraseña", e);
        }
    }
}
