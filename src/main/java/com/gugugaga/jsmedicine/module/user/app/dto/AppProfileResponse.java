package com.gugugaga.jsmedicine.module.user.app.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import com.gugugaga.jsmedicine.common.enums.UserAuthProvider;

public record AppProfileResponse(
        Long id,
        String username,
        String mobile,
        String email,
        String nickname,
        String avatarUrl,
        UserAuthProvider authProvider,
        Gender gender,
        EnabledStatus status,
        Boolean profileCompleted,
        Long studentId,
        StudentCertificationStatus certificationStatus
) {
}
