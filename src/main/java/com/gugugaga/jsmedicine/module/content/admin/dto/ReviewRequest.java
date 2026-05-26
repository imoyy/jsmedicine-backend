package com.gugugaga.jsmedicine.module.content.admin.dto;

import com.gugugaga.jsmedicine.common.enums.ReviewStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ReviewRequest(
        @NotNull(message = "reviewStatus must not be null")
        ReviewStatus reviewStatus,

        @Size(max = 512, message = "comment length must be less than 512")
        String comment
) {
}
