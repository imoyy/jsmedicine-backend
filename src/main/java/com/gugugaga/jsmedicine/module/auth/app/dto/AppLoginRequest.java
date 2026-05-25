package com.gugugaga.jsmedicine.module.auth.app.dto;

import jakarta.validation.constraints.NotBlank;

public record AppLoginRequest(
        @NotBlank(message = "username must not be blank")
        String username,
        @NotBlank(message = "password must not be blank")
        String password
) {
}
