package com.gugugaga.jsmedicine.module.expert.app.dto;

import com.gugugaga.jsmedicine.common.enums.Gender;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

public record AppExpertCertificationRequest(
        @NotBlank(message = "realName must not be blank")
        @Size(max = 64, message = "realName length must be less than 64")
        String realName,

        Gender gender,

        LocalDate birthDate,

        @Size(max = 32, message = "mobile length must be less than 32")
        String mobile,

        @Size(max = 128, message = "title length must be less than 128")
        String title,

        @Size(max = 128, message = "organization length must be less than 128")
        String organization,

        Long organizationId,

        Long practiceTypeId,

        @Size(max = 255, message = "specialty length must be less than 255")
        String specialty,

        String introduction,

        @Size(max = 512, message = "consultationNotice length must be less than 512")
        String consultationNotice,

        @NotEmpty(message = "categoryIds must not be empty")
        List<Long> categoryIds,

        @Valid
        List<AppExpertCertificationFileRequest> certificationFiles
) {
}
