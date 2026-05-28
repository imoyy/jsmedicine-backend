package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record PracticeTypeResponse(
        Long id,
        Long parentId,
        String typeCode,
        String typeName,
        EnabledStatus status,
        Integer sortOrder
) {
}
