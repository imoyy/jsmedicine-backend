package com.gugugaga.jsmedicine.module.content.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record FileAssetRequest(
        @NotBlank(message = "assetType must not be blank")
        @Size(max = 32, message = "assetType length must be less than 32")
        String assetType,

        @Size(max = 32, message = "storageProvider length must be less than 32")
        String storageProvider,

        @Size(max = 128, message = "bucketName length must be less than 128")
        String bucketName,

        @NotBlank(message = "objectKey must not be blank")
        @Size(max = 512, message = "objectKey length must be less than 512")
        String objectKey,

        @Size(max = 255, message = "originalName length must be less than 255")
        String originalName,

        @Size(max = 128, message = "contentType length must be less than 128")
        String contentType,

        Long fileSize,

        @Size(max = 512, message = "url length must be less than 512")
        String url
) {
}
