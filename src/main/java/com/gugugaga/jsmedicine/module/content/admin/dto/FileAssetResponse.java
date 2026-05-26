package com.gugugaga.jsmedicine.module.content.admin.dto;

public record FileAssetResponse(
        Long id,
        String assetType,
        String storageProvider,
        String bucketName,
        String objectKey,
        String originalName,
        String contentType,
        Long fileSize,
        String url,
        Long createdBy
) {
}
