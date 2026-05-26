package com.gugugaga.jsmedicine.module.system.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record SysRoleUpsertRequest(
        @NotBlank(message = "roleCode must not be blank")
        @Size(max = 64, message = "roleCode length must be less than 64")
        String roleCode,

        @NotBlank(message = "roleName must not be blank")
        @Size(max = 64, message = "roleName length must be less than 64")
        String roleName,

        @Size(max = 255, message = "description length must be less than 255")
        String description,

        @NotNull(message = "status must not be null")
        EnabledStatus status,

        Integer sortOrder
) {
}
