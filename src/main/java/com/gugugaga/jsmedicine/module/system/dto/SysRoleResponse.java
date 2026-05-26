package com.gugugaga.jsmedicine.module.system.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record SysRoleResponse(
        Long id,
        String roleCode,
        String roleName,
        String description,
        EnabledStatus status,
        Integer sortOrder,
        long permissionCount
) {
}
