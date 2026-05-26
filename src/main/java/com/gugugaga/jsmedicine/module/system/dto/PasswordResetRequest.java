package com.gugugaga.jsmedicine.module.system.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record PasswordResetRequest(
        @NotBlank(message = "password must not be blank")
        @Size(min = 8, max = 64, message = "password length must be between 8 and 64")
        String password
) {
}
