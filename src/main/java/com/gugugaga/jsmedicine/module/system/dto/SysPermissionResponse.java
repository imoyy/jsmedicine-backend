package com.gugugaga.jsmedicine.module.system.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.PermissionType;

public record SysPermissionResponse(
        Long id,
        Long parentId,
        String permissionCode,
        String permissionName,
        PermissionType permissionType,
        String routePath,
        String apiMethod,
        String apiPath,
        String icon,
        Integer sortOrder,
        EnabledStatus status
) {
}
