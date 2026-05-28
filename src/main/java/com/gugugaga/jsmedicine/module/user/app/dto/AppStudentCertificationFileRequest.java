package com.gugugaga.jsmedicine.module.user.app.dto;

import jakarta.validation.constraints.Size;

public record AppStudentCertificationFileRequest(
        Long fileAssetId,

        @Size(max = 1024, message = "sourceUrl length must be less than 1024")
        String sourceUrl,

        @Size(max = 32, message = "materialType length must be less than 32")
        String materialType,

        Integer sortOrder
) {
}
