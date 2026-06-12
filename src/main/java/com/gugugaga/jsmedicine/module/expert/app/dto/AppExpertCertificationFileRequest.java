package com.gugugaga.jsmedicine.module.expert.app.dto;

import jakarta.validation.constraints.Size;

public record AppExpertCertificationFileRequest(
        Long fileAssetId,

        @Size(max = 1024, message = "sourceUrl length must be less than 1024")
        String sourceUrl,

        @Size(max = 64, message = "materialType length must be less than 64")
        String materialType,

        Integer sortOrder
) {
}
