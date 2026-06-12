package com.gugugaga.jsmedicine.module.expert.app.dto;

public record AppExpertCertificationFileResponse(
        Long id,
        Long fileAssetId,
        String sourceUrl,
        String materialType,
        Integer sortOrder
) {
}
