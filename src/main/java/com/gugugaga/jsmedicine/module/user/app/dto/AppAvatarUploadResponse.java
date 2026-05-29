package com.gugugaga.jsmedicine.module.user.app.dto;

import java.time.LocalDateTime;

public record AppAvatarUploadResponse(
        String method,
        String uploadUrl,
        String bucketName,
        String objectKey,
        String contentType,
        Long fileSize,
        LocalDateTime expiresAt
) {
}
