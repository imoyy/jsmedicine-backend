package com.gugugaga.jsmedicine.module.content.admin.dto;

import java.time.LocalDateTime;

public record AdminCoverUploadResponse(
        String usage,
        String method,
        String uploadUrl,
        String bucketName,
        String objectKey,
        String contentType,
        Long fileSize,
        LocalDateTime expiresAt
) {
}
