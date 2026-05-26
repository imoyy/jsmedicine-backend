package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpertRequest(
        @NotBlank(message = "realName must not be blank")
        @Size(max = 64, message = "realName length must be less than 64")
        String realName,

        @Size(max = 512, message = "avatarUrl length must be less than 512")
        String avatarUrl,

        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @Size(max = 128, message = "organization length must be less than 128")
        String organization,

        @Size(max = 255, message = "specialty length must be less than 255")
        String specialty,

        String introduction,

        @NotNull(message = "status must not be null")
        EnabledStatus status,

        @NotNull(message = "consultEnabled must not be null")
        EnabledStatus consultEnabled,

        @Size(max = 512, message = "consultationNotice length must be less than 512")
        String consultationNotice,

        Integer sortOrder
) {
}
