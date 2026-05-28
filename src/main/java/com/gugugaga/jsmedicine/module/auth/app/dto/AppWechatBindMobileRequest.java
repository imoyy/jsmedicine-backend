package com.gugugaga.jsmedicine.module.auth.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AppWechatBindMobileRequest(
        @NotBlank(message = "bindToken must not be blank")
        @Size(max = 64, message = "bindToken length must be less than 64")
        String bindToken,

        @NotBlank(message = "mobile must not be blank")
        @Pattern(regexp = "^1\\d{10}$", message = "mobile format is invalid")
        String mobile,

        @NotBlank(message = "code must not be blank")
        @Pattern(regexp = "^\\d{4,8}$", message = "code format is invalid")
        String code
) {
}
