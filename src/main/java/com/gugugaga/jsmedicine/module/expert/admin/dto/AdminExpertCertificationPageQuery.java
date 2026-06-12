package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.ExpertCertificationStatus;

public record AdminExpertCertificationPageQuery(
        long page,
        long size,
        String sort,
        String keyword,
        ExpertCertificationStatus certificationStatus
) {
}
