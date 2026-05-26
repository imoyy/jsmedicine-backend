package com.gugugaga.jsmedicine.module.user.app.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AppStudentCertificationRequest(
        @NotBlank(message = "realName must not be blank")
        @Size(max = 64, message = "realName length must be less than 64")
        String realName,

        @Size(max = 32, message = "mobile length must be less than 32")
        String mobile,

        @Size(max = 32, message = "idCardNo length must be less than 32")
        String idCardNo,

        @Size(max = 64, message = "province length must be less than 64")
        String province,

        @Size(max = 64, message = "city length must be less than 64")
        String city,

        @Size(max = 64, message = "district length must be less than 64")
        String district,

        @Size(max = 128, message = "organization length must be less than 128")
        String organization,

        @Size(max = 128, message = "positionTitle length must be less than 128")
        String positionTitle,

        String certificationMaterials
) {
}
