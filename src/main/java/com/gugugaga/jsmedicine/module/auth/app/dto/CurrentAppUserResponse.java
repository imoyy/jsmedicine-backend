package com.gugugaga.jsmedicine.module.auth.app.dto;

import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import com.gugugaga.jsmedicine.module.expert.app.dto.AppCurrentExpertResponse;

import java.util.List;

public record CurrentAppUserResponse(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        String mobile,
        String email,
        Boolean profileCompleted,
        Long studentId,
        StudentCertificationStatus certificationStatus,
        List<String> identities,
        AppCurrentExpertResponse expertMode
) {
}
