package com.gugugaga.jsmedicine.module.content.admin.dto;

public record AdminCoverAssetResponse(
        Long fileAssetId,
        String usage,
        String coverUrl,
        String bucketName,
        String objectKey,
        String originalName,
        String contentType,
        Long fileSize
) {
}
