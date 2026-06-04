package com.gugugaga.jsmedicine.module.content.admin.service;

import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.infrastructure.security.CurrentAdminAccessor;
import com.gugugaga.jsmedicine.infrastructure.storage.StorageClient;
import com.gugugaga.jsmedicine.infrastructure.storage.StorageObjectStat;
import com.gugugaga.jsmedicine.infrastructure.storage.StorageProperties;
import com.gugugaga.jsmedicine.infrastructure.storage.entity.FileAsset;
import com.gugugaga.jsmedicine.infrastructure.storage.mapper.FileAssetMapper;
import com.gugugaga.jsmedicine.module.content.admin.dto.AdminCoverAssetResponse;
import com.gugugaga.jsmedicine.module.content.admin.dto.AdminCoverConfirmRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.AdminCoverUploadRequest;
import com.gugugaga.jsmedicine.module.content.admin.dto.AdminCoverUploadResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class AdminCoverUploadService {

    private static final String IMAGE_ASSET_TYPE = "image";
    private static final String PUBLIC_FILE_URL_TEMPLATE = "/api/v1/files/%d/content";
    private static final DateTimeFormatter COVER_PATH_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");
    private static final Map<String, String> COVER_CONTENT_TYPE_EXTENSIONS = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp"
    );
    private static final Map<String, String> COVER_USAGES = createCoverUsages();

    private final FileAssetMapper fileAssetMapper;
    private final StorageClient storageClient;
    private final StorageProperties storageProperties;
    private final CurrentAdminAccessor currentAdminAccessor;

    public AdminCoverUploadService(
            FileAssetMapper fileAssetMapper,
            StorageClient storageClient,
            StorageProperties storageProperties,
            CurrentAdminAccessor currentAdminAccessor
    ) {
        this.fileAssetMapper = fileAssetMapper;
        this.storageClient = storageClient;
        this.storageProperties = storageProperties;
        this.currentAdminAccessor = currentAdminAccessor;
    }

    public AdminCoverUploadResponse createUploadUrl(AdminCoverUploadRequest request) {
        validateUploadRequest(request);
        String usage = normalizeUsage(request.usage());
        String normalizedContentType = normalizeContentType(request.contentType());
        String extension = resolveCoverExtension(request.originalName(), normalizedContentType);
        String objectKey = buildCoverObjectKey(usage, extension);
        var uploadUrl = storageClient.createPresignedUploadUrl(
                storageProperties.getCover().getBucketName(),
                objectKey,
                Duration.ofSeconds(storageProperties.getCover().getUploadUrlTtlSeconds())
        );
        return new AdminCoverUploadResponse(
                usage,
                uploadUrl.method(),
                uploadUrl.url(),
                storageProperties.getCover().getBucketName(),
                objectKey,
                normalizedContentType,
                request.fileSize(),
                uploadUrl.expiresAt()
        );
    }

    @Transactional(rollbackFor = Exception.class)
    public AdminCoverAssetResponse confirmUpload(AdminCoverConfirmRequest request) {
        String usage = normalizeUsage(request.usage());
        String objectKey = normalizeObjectKey(request.objectKey());
        if (!isExpectedCoverObjectKey(usage, objectKey)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cover objectKey is invalid");
        }
        StorageObjectStat objectStat = storageClient.statObject(storageProperties.getCover().getBucketName(), objectKey);
        validateObjectStat(objectStat);

        FileAsset fileAsset = new FileAsset();
        fileAsset.setAssetType(IMAGE_ASSET_TYPE);
        fileAsset.setStorageProvider(storageProperties.getProvider());
        fileAsset.setBucketName(storageProperties.getCover().getBucketName());
        fileAsset.setObjectKey(objectKey);
        fileAsset.setOriginalName(hasText(request.originalName()) ? request.originalName() : extractObjectName(objectKey));
        fileAsset.setContentType(normalizeContentType(objectStat.contentType()));
        fileAsset.setFileSize(objectStat.contentLength());
        fileAsset.setCreatedBy(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        fileAsset.setDeleted(0);
        fileAssetMapper.insert(fileAsset);

        String coverUrl = buildPublicFileUrl(fileAsset.getId());
        fileAsset.setUrl(coverUrl);
        fileAssetMapper.updateById(fileAsset);

        return new AdminCoverAssetResponse(
                fileAsset.getId(),
                usage,
                coverUrl,
                fileAsset.getBucketName(),
                fileAsset.getObjectKey(),
                fileAsset.getOriginalName(),
                fileAsset.getContentType(),
                fileAsset.getFileSize()
        );
    }

    private void validateUploadRequest(AdminCoverUploadRequest request) {
        long fileSize = request.fileSize() == null ? 0 : request.fileSize();
        if (fileSize < 1 || fileSize > storageProperties.getCover().getMaxFileSizeBytes()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cover file size exceeds limit");
        }
        String contentType = normalizeContentType(request.contentType());
        if (!storageProperties.getCover().getAllowedContentTypes().contains(contentType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Cover content type is not supported");
        }
        normalizeUsage(request.usage());
    }

    private void validateObjectStat(StorageObjectStat objectStat) {
        if (objectStat.contentLength() < 1 || objectStat.contentLength() > storageProperties.getCover().getMaxFileSizeBytes()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Uploaded cover file size exceeds limit");
        }
        String contentType = normalizeContentType(objectStat.contentType());
        if (!storageProperties.getCover().getAllowedContentTypes().contains(contentType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Uploaded cover content type is not supported");
        }
    }

    private String normalizeUsage(String usage) {
        String normalizedUsage = usage == null ? "" : usage.trim().toLowerCase(Locale.ROOT);
        if (!COVER_USAGES.containsKey(normalizedUsage)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported cover usage: " + usage);
        }
        return normalizedUsage;
    }

    private String buildCoverObjectKey(String usage, String extension) {
        return storageProperties.getCover().getObjectPrefix()
                + "/"
                + COVER_USAGES.get(usage)
                + "/"
                + LocalDateTime.now().format(COVER_PATH_MONTH_FORMATTER)
                + "/"
                + UUID.randomUUID()
                + extension;
    }

    private boolean isExpectedCoverObjectKey(String usage, String objectKey) {
        String expectedPrefix = storageProperties.getCover().getObjectPrefix() + "/" + COVER_USAGES.get(usage) + "/";
        return objectKey.startsWith(expectedPrefix);
    }

    private String resolveCoverExtension(String originalName, String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        String extension = extractExtension(originalName);
        if (!hasText(extension)) {
            return COVER_CONTENT_TYPE_EXTENSIONS.get(normalizedContentType);
        }
        if (".jpeg".equals(extension)) {
            extension = ".jpg";
        }
        String expectedExtension = COVER_CONTENT_TYPE_EXTENSIONS.get(normalizedContentType);
        if (!Objects.equals(extension, expectedExtension)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Cover file extension does not match content type");
        }
        return extension;
    }

    private String extractExtension(String originalName) {
        if (!hasText(originalName)) {
            return "";
        }
        int index = originalName.lastIndexOf('.');
        if (index < 0 || index == originalName.length() - 1) {
            return "";
        }
        return originalName.substring(index).toLowerCase(Locale.ROOT);
    }

    private String normalizeContentType(String contentType) {
        return contentType == null ? "" : contentType.trim().toLowerCase(Locale.ROOT);
    }

    private String normalizeObjectKey(String objectKey) {
        return objectKey == null ? "" : objectKey.trim();
    }

    private String extractObjectName(String objectKey) {
        int separatorIndex = objectKey.lastIndexOf('/');
        return separatorIndex < 0 ? objectKey : objectKey.substring(separatorIndex + 1);
    }

    private String buildPublicFileUrl(Long fileAssetId) {
        String relativePath = PUBLIC_FILE_URL_TEMPLATE.formatted(fileAssetId);
        if (!hasText(storageProperties.getPublicBaseUrl())) {
            return relativePath;
        }
        return storageProperties.getPublicBaseUrl().replaceAll("/+$", "") + relativePath;
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private static Map<String, String> createCoverUsages() {
        Map<String, String> usages = new LinkedHashMap<>();
        usages.put("article-cover", "article");
        usages.put("course-cover", "course");
        usages.put("book-cover", "book");
        usages.put("podcast-cover", "podcast");
        usages.put("topic-cover", "topic");
        usages.put("live-cover", "live");
        usages.put("expert-cover", "expert");
        usages.put("knowledge-cover", "knowledge");
        usages.put("home-content-cover", "home-content");
        return usages;
    }
}
