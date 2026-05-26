package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record StudentCertificationReviewRequest(
        @NotNull(message = "certificationStatus must not be null")
        StudentCertificationStatus certificationStatus,

        @Size(max = 512, message = "rejectReason length must be less than 512")
        String rejectReason
) {
}
