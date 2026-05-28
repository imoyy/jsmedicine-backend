package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminUserUpdateRequest(
        @NotBlank(message = "nickname must not be blank")
        @Size(max = 64, message = "nickname length must be less than 64")
        String nickname,

        @NotBlank(message = "profileSignature must not be blank")
        @Size(max = 255, message = "profileSignature length must be less than 255")
        String profileSignature,

        @NotNull(message = "status must not be null")
        EnabledStatus status,

        @NotNull(message = "role must not be null")
        AppUserManagementRole role,

        Long studentId,

        @Size(max = 64, message = "province length must be less than 64")
        String province,

        @Size(max = 32, message = "provinceCode length must be less than 32")
        String provinceCode,

        @Size(max = 64, message = "city length must be less than 64")
        String city,

        @Size(max = 32, message = "cityCode length must be less than 32")
        String cityCode,

        @Size(max = 64, message = "district length must be less than 64")
        String district,

        @Size(max = 32, message = "districtCode length must be less than 32")
        String districtCode,

        @Size(max = 128, message = "organization length must be less than 128")
        String organization,

        Long organizationId,

        Long practiceTypeId
) {
}
