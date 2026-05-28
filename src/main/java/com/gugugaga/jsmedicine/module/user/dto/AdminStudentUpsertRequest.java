package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AdminStudentUpsertRequest(
        @Size(max = 64, message = "studentNo length must be less than 64")
        String studentNo,

        @NotBlank(message = "realName must not be blank")
        @Size(max = 64, message = "realName length must be less than 64")
        String realName,

        @NotNull(message = "gender must not be null")
        Gender gender,

        @NotNull(message = "age must not be null")
        @Min(value = 0, message = "age must be greater than or equal to 0")
        Integer age,

        @Size(max = 64, message = "educationLevel length must be less than 64")
        String educationLevel,

        @Size(max = 32, message = "mobile length must be less than 32")
        String mobile,

        @Size(max = 32, message = "idCardNo length must be less than 32")
        String idCardNo,

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

        @Size(max = 128, message = "positionTitle length must be less than 128")
        String positionTitle,

        Long practiceTypeId,

        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
