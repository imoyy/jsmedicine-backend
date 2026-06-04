package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record ExpertRequest(
        Long userId,

        @NotBlank(message = "realName must not be blank")
        @Size(max = 64, message = "realName length must be less than 64")
        String realName,

        Gender gender,

        LocalDate birthDate,

        @Size(max = 32, message = "mobile length must be less than 32")
        String mobile,

        @Size(max = 512, message = "avatarUrl length must be less than 512")
        String avatarUrl,

        @Size(max = 512, message = "coverUrl length must be less than 512")
        @Schema(description = "封面稳定读取地址，只能填写管理端封面上传接口返回的 /api/v1/files/{id}/content", example = "/api/v1/files/107/content")
        String coverUrl,

        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @Size(max = 128, message = "organization length must be less than 128")
        String organization,

        Long organizationId,

        @Size(max = 255, message = "specialty length must be less than 255")
        String specialty,

        Long practiceTypeId,

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
