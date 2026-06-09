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
        return resolveCoverUrl(coverUrl, null);
    }

    public String resolveCoverUrl(String requestedCoverUrl, String currentCoverUrl) {
        return resolveCoverBinding(requestedCoverUrl, currentCoverUrl, null).coverUrl();
    }

    public CoverBinding resolvePublicImageBinding(
            String requestedUrl,
            String currentUrl,
            Long currentFileAssetId,
            String fieldName
    ) {
        String normalizedRequestedUrl = normalizeNullable(requestedUrl);
        String normalizedCurrentUrl = normalizeNullable(currentUrl);
        if (normalizedRequestedUrl.equals(normalizedCurrentUrl)) {
            return new CoverBinding(currentFileAssetId, requestedUrl);
        }
        if (!hasText(requestedUrl)) {
            return new CoverBinding(null, requestedUrl);
        }
        return requireStableImageUrlChange(requestedUrl, fieldName);
    }

    public CoverBinding resolveCoverBinding(
            String requestedCoverUrl,
            String currentCoverUrl,
            Long currentCoverFileAssetId
    ) {
        return resolvePublicImageBinding(
                requestedCoverUrl,
                currentCoverUrl,
                currentCoverFileAssetId,
                "coverUrl"
        );
    }

    private CoverBinding requireStableImageUrlChange(String imageUrl, String fieldName) {
        if (!hasText(imageUrl)) {
            return new CoverBinding(null, imageUrl);
        }
        String normalizedImageUrl = imageUrl.trim();
        Long fileAssetId = extractFileAssetId(normalizedImageUrl);
        if (fileAssetId == null) {
            throw invalidImageUrl(fieldName);
        }
        FileAsset fileAsset = fileAssetMapper.selectById(fileAssetId);
        if (!isStableCoverAsset(fileAsset)) {
            throw invalidImageUrl(fieldName);
        }
        String relativeUrl = buildRelativeFileUrl(fileAssetId);
        String absoluteUrl = buildAbsoluteFileUrl(relativeUrl);
        if (!normalizedImageUrl.equals(relativeUrl)
                && !normalizedImageUrl.equals(absoluteUrl)
                && !normalizedImageUrl.equals(normalizeNullable(fileAsset.getUrl()))) {
            throw invalidImageUrl(fieldName);
        }
        return new CoverBinding(fileAssetId, hasText(fileAsset.getUrl()) ? fileAsset.getUrl().trim() : relativeUrl);
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
        return invalidImageUrl("coverUrl");
    }

    private BusinessException invalidImageUrl(String fieldName) {
        return new BusinessException(ErrorCode.BAD_REQUEST, fieldName + " must be generated through cover upload APIs");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    public record CoverBinding(Long fileAssetId, String coverUrl) {
    }
}
