package com.gugugaga.jsmedicine.infrastructure.storage.service;

import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.infrastructure.storage.StorageProperties;
import com.gugugaga.jsmedicine.infrastructure.storage.entity.FileAsset;
import com.gugugaga.jsmedicine.infrastructure.storage.mapper.FileAssetMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class StableCoverUrlService {

    private static final String PUBLIC_FILE_URL_PREFIX = "/api/v1/files/";
    private static final String PUBLIC_FILE_URL_SUFFIX = "/content";

    private final FileAssetMapper fileAssetMapper;
    private final StorageProperties storageProperties;

    public StableCoverUrlService(FileAssetMapper fileAssetMapper, StorageProperties storageProperties) {
        this.fileAssetMapper = fileAssetMapper;
        this.storageProperties = storageProperties;
    }

    public String requireStableCoverUrl(String coverUrl) {
        if (!hasText(coverUrl)) {
            return coverUrl;
        }
        String normalizedCoverUrl = coverUrl.trim();
        Long fileAssetId = extractFileAssetId(normalizedCoverUrl);
        if (fileAssetId == null) {
            throw invalidCoverUrl();
        }
        FileAsset fileAsset = fileAssetMapper.selectById(fileAssetId);
        if (!isStableCoverAsset(fileAsset)) {
            throw invalidCoverUrl();
        }
        String relativeUrl = buildRelativeFileUrl(fileAssetId);
        String absoluteUrl = buildAbsoluteFileUrl(relativeUrl);
        if (!normalizedCoverUrl.equals(relativeUrl)
                && !normalizedCoverUrl.equals(absoluteUrl)
                && !normalizedCoverUrl.equals(normalizeNullable(fileAsset.getUrl()))) {
            throw invalidCoverUrl();
        }
        return hasText(fileAsset.getUrl()) ? fileAsset.getUrl().trim() : relativeUrl;
    }

    private Long extractFileAssetId(String coverUrl) {
        int prefixIndex = coverUrl.indexOf(PUBLIC_FILE_URL_PREFIX);
        if (prefixIndex < 0 || !coverUrl.endsWith(PUBLIC_FILE_URL_SUFFIX)) {
            return null;
        }
        String resourcePath = coverUrl.substring(prefixIndex);
        if (!resourcePath.startsWith(PUBLIC_FILE_URL_PREFIX) || !resourcePath.endsWith(PUBLIC_FILE_URL_SUFFIX)) {
            return null;
        }
        String idPart = resourcePath.substring(
                PUBLIC_FILE_URL_PREFIX.length(),
                resourcePath.length() - PUBLIC_FILE_URL_SUFFIX.length()
        );
        if (idPart.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(idPart);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private boolean isStableCoverAsset(FileAsset fileAsset) {
        return fileAsset != null
                && Objects.equals(fileAsset.getDeleted(), 0)
                && "image".equalsIgnoreCase(fileAsset.getAssetType())
                && hasText(fileAsset.getObjectKey())
                && fileAsset.getObjectKey().startsWith(storageProperties.getCover().getObjectPrefix() + "/");
    }

    private String buildRelativeFileUrl(Long fileAssetId) {
        return PUBLIC_FILE_URL_PREFIX + fileAssetId + PUBLIC_FILE_URL_SUFFIX;
    }

    private String buildAbsoluteFileUrl(String relativeUrl) {
        if (!hasText(storageProperties.getPublicBaseUrl())) {
            return "";
        }
        return storageProperties.getPublicBaseUrl().replaceAll("/+$", "") + relativeUrl;
    }

    private String normalizeNullable(String value) {
        return value == null ? "" : value.trim();
    }

    private BusinessException invalidCoverUrl() {
        return new BusinessException(ErrorCode.BAD_REQUEST, "coverUrl must be generated through cover upload APIs");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
