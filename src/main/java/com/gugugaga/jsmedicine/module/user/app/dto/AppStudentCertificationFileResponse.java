package com.gugugaga.jsmedicine.module.user.app.dto;

public record AppStudentCertificationFileResponse(
        Long id,
        Long fileAssetId,
        String sourceUrl,
        String materialType,
        Integer sortOrder
) {
}
