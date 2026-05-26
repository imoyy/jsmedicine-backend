package com.gugugaga.jsmedicine.module.system.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SysAdminUpsertRequest(
        @NotBlank(message = "username must not be blank")
        @Size(max = 64, message = "username length must be less than 64")
        String username,

        @Size(max = 64, message = "password length must be less than 64")
        String password,

        @NotBlank(message = "realName must not be blank")
        @Size(max = 64, message = "realName length must be less than 64")
        String realName,

        @Size(max = 32, message = "mobile length must be less than 32")
        String mobile,

        @Email(message = "email format is invalid")
        @Size(max = 128, message = "email length must be less than 128")
        String email,

        @Size(max = 512, message = "avatarUrl length must be less than 512")
        String avatarUrl,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
