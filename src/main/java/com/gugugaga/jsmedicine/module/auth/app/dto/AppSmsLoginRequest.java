package com.gugugaga.jsmedicine.module.auth.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record AppSmsLoginRequest(
        @NotBlank(message = "mobile must not be blank")
        @Pattern(regexp = "^1\\d{10}$", message = "mobile format is invalid")
        String mobile,

        @NotBlank(message = "code must not be blank")
        @Pattern(regexp = "^\\d{4,8}$", message = "code format is invalid")
        String code
) {
}
