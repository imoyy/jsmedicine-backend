package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record OrganizationUpsertRequest(
        @Size(max = 64, message = "orgCode length must be less than 64")
        String orgCode,

        @NotBlank(message = "orgName must not be blank")
        @Size(max = 128, message = "orgName length must be less than 128")
        String orgName,

        @Size(max = 32, message = "orgType length must be less than 32")
        String orgType,

        @Size(max = 32, message = "provinceCode length must be less than 32")
        String provinceCode,

        @Size(max = 32, message = "cityCode length must be less than 32")
        String cityCode,

        @Size(max = 32, message = "districtCode length must be less than 32")
        String districtCode,

        @Size(max = 255, message = "address length must be less than 255")
        String address,

        @NotNull(message = "status must not be null")
        EnabledStatus status,

        @Min(value = 0, message = "sortOrder must be greater than or equal to 0")
        Integer sortOrder
) {
}
