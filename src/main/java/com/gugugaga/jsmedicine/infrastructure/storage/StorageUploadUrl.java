package com.gugugaga.jsmedicine.infrastructure.storage;

import java.time.LocalDateTime;

public record StorageUploadUrl(
        String method,
        String url,
        LocalDateTime expiresAt
) {
}
