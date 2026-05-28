package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;

import java.time.LocalDateTime;
import java.util.List;

public record AdminStudentResponse(
        Long id,
        Long userId,
        String studentNo,
        String realName,
        String mobile,
        String idCardNo,
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
        Long certificationReviewedBy,
        String rejectReason,
        String certificationMaterials,
        List<StudentCertificationFileResponse> certificationFiles,
        LocalDateTime enrolledAt
) {
}
