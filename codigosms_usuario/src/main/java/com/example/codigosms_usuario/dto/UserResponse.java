package com.example.codigosms_usuario.dto;

import java.time.LocalDateTime;
import java.util.List;

import com.example.codigosms_usuario.model.Role;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserResponse {

    private Long userId;

    private String name;

    private String email;

    private Role role;

    private Boolean active;

    private LocalDateTime createdAt;

    private List<AddressResponse> addresses;
}
