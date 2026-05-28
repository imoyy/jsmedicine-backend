package com.gugugaga.jsmedicine.module.user.dto;

public record StudentCertificationFileResponse(
        Long id,
        Long fileAssetId,
        String sourceUrl,
        String materialType,
        Integer sortOrder
) {
}
