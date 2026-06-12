package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.ExpertCertificationStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppExpertCertificationFileResponse;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record AdminExpertCertificationResponse(
        Long id,
        Long userId,
        String realName,
        Gender gender,
        LocalDate birthDate,
        String mobile,
        String title,
        String organization,
        Long organizationId,
        Long practiceTypeId,
        String specialty,
        String introduction,
        String consultationNotice,
        List<Long> categoryIds,
        ExpertCertificationStatus certificationStatus,
        LocalDateTime certificationSubmittedAt,
        LocalDateTime certificationReviewedAt,
        Long certificationReviewedBy,
        String rejectReason,
        List<AppExpertCertificationFileResponse> certificationFiles
) {
}
