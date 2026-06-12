package com.gugugaga.jsmedicine.module.expert.admin.dto;

import com.gugugaga.jsmedicine.common.enums.ExpertCertificationStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExpertCertificationReviewRequest(
        @NotNull(message = "certificationStatus must not be null")
        ExpertCertificationStatus certificationStatus,

        @Size(max = 512, message = "rejectReason length must be less than 512")
        String rejectReason
) {
}
