package com.gugugaga.jsmedicine.module.user.app.dto;

import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;

public record AppProfileSummaryResponse(
        AppProfileResponse profile,
        StudentCertificationStatus certificationStatus,
        long favoriteCount,
        long browseHistoryCount
) {
}
