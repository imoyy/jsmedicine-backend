package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;

import java.time.LocalDateTime;

public record AdminStudentResponse(
        Long id,
        Long userId,
        String studentNo,
        String realName,
        String mobile,
        String idCardNo,
        String province,
        String city,
        String district,
        String organization,
        String positionTitle,
        EnabledStatus status,
        StudentCertificationStatus certificationStatus,
        LocalDateTime certificationSubmittedAt,
        LocalDateTime certificationReviewedAt,
        Long certificationReviewedBy,
        String rejectReason,
        String certificationMaterials,
        LocalDateTime enrolledAt
) {
}
