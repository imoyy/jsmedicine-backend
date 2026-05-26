package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;

public record AdminStudentPageQuery(
        long page,
        long size,
        String sort,
        String keyword,
        EnabledStatus status,
        StudentCertificationStatus certificationStatus
) {
}
