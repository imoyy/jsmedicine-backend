package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;

public record OrganizationResponse(
        Long id,
        String orgCode,
        String orgName,
        String orgType,
        String provinceCode,
        String cityCode,
        String districtCode,
        String address,
        EnabledStatus status,
        Integer sortOrder
) {
}
