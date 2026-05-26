package com.gugugaga.jsmedicine.module.auth.app.dto;

import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;

public record CurrentAppUserResponse(
        Long id,
        String username,
        String nickname,
        String avatarUrl,
        String mobile,
        String email,
        Boolean profileCompleted,
        Long studentId,
        StudentCertificationStatus certificationStatus
) {
}
