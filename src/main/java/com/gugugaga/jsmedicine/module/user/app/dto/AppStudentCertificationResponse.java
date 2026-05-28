package com.gugugaga.jsmedicine.module.user.app.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AppStudentCertificationResponse(
        Long studentId,
        String studentNo,
        String realName,
        String mobile,
        String province,
        String provinceCode,
        String city,
        String cityCode,
        String district,
        String districtCode,
        String organization,
        Long organizationId,
        String positionTitle,
        Long practiceTypeId,
        EnabledStatus status,
        StudentCertificationStatus certificationStatus,
        LocalDateTime certificationSubmittedAt,
        LocalDateTime certificationReviewedAt,
        String rejectReason,
        String certificationMaterials,
        List<AppStudentCertificationFileResponse> certificationFiles,
        LocalDateTime enrolledAt
) {
}
