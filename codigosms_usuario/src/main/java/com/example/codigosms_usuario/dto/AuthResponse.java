package com.example.codigosms_usuario.dto;

import com.example.codigosms_usuario.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuthResponse {

    private Long userId;

    private String email;

    private Role role;

    private String message;
}
