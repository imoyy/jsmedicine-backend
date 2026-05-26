package com.gugugaga.jsmedicine.module.user.app.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;

import java.time.LocalDateTime;

public record AppStudentCertificationResponse(
        Long studentId,
        String studentNo,
        String realName,
        String mobile,
        String province,
        String city,
        String district,
        String organization,
        String positionTitle,
        EnabledStatus status,
        StudentCertificationStatus certificationStatus,
        LocalDateTime certificationSubmittedAt,
        LocalDateTime certificationReviewedAt,
        String rejectReason,
        String certificationMaterials,
        LocalDateTime enrolledAt
) {
}
