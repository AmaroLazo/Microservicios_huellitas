package com.example.codigosms_usuario.dto;

import com.example.codigosms_usuario.model.Role;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminUserRequest extends UserRequest {
    @NotNull
    private Role role;

    @NotNull
    private Boolean active;
}
