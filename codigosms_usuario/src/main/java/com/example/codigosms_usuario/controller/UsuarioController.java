package com.example.codigosms_usuario.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.codigosms_usuario.dto.AuthResponse;
import com.example.codigosms_usuario.dto.LoginRequest;
import com.example.codigosms_usuario.dto.PasswordRequest;
import com.example.codigosms_usuario.dto.ResetRequest;
import com.example.codigosms_usuario.dto.UsuarioRequest;
import com.example.codigosms_usuario.dto.UsuarioResponse;
import com.example.codigosms_usuario.model.Usuario;
import com.example.codigosms_usuario.service.UsuarioService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {
    private final UsuarioService service;

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .usuarioId(u.getId())
                .nombre(u.getNombre())
                .email(u.getEmail())
                .rol(u.getRol())
                .activo(u.getActivo())
                .fechaCreacion(u.getFechaCreacion())
                .build();
    }

    @PostMapping
    public ResponseEntity<UsuarioResponse> registrarUsuario(@Valid @RequestBody UsuarioRequest dto){
        return ResponseEntity.status(201).body(toResponse(service.crearUsuario(dto)));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest dto) {
        return ResponseEntity.ok(service.autenticar(dto.getEmail(), dto.getPassword()));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        // Sin sesiones ni tokens JWT no hay nada que invalidar en el servidor
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<UsuarioResponse> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(toResponse(service.obtenerDetalleUsuario(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<UsuarioResponse> actualizarPerfil(@PathVariable Long id,
                                                            @Valid @RequestBody UsuarioRequest dto) {
        return ResponseEntity.ok(toResponse(service.actualizarUsuario(id, dto)));
    }

    @PutMapping("/{id}/password")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id,
                                                @RequestBody PasswordRequest dto) {
        service.cambiarPassword(id, dto.getPasswordActual(), dto.getPasswordNueva());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/solicitar")
    public ResponseEntity<Void> solicitarResetPassword(@RequestParam String email) {
        service.crearTokenReset(email);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/password-reset/confirmar")
    public ResponseEntity<Void> resetPassword(@RequestBody ResetRequest dto) {
        service.resetPassword(dto.getToken(), dto.getPasswordNueva());
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/desactivar")
    public ResponseEntity<Void> desactivarCuenta(@PathVariable Long id) {
        service.setActivo(id, false);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<UsuarioResponse>> listarUsuarios(){
        List<UsuarioResponse> lista = service.listarUsuarios().stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(lista);
    }

}
