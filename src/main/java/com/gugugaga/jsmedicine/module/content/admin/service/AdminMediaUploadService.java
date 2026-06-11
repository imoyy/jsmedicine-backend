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
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

@Service
public class AdminMediaUploadService {

    private static final String VIDEO_ASSET_TYPE = "video";
    private static final String AUDIO_ASSET_TYPE = "audio";
    private static final String PUBLIC_FILE_URL_TEMPLATE = "/api/v1/files/%d/content";
    private static final DateTimeFormatter MEDIA_PATH_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy/MM");

    private static final Map<String, String> VIDEO_CONTENT_TYPE_EXTENSIONS = Map.of(
            "video/mp4", ".mp4",
            "video/webm", ".webm",
            "video/ogg", ".ogg",
            "video/x-msvideo", ".avi",
            "video/quicktime", ".mov"
    );

    private static final Map<String, String> AUDIO_CONTENT_TYPE_EXTENSIONS = Map.of(
            "audio/mpeg", ".mp3",
            "audio/mp3", ".mp3",
            "audio/wav", ".wav",
            "audio/ogg", ".ogg",
            "audio/aac", ".aac",
            "audio/flac", ".flac",
            "audio/x-m4a", ".m4a"
    );

    private static final Map<String, String> MEDIA_USAGES = createMediaUsages();

    private final FileAssetMapper fileAssetMapper;
    private final StorageClient storageClient;
    private final StorageProperties storageProperties;
    private final CurrentAdminAccessor currentAdminAccessor;

    public AdminMediaUploadService(
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
        String extension = resolveMediaExtension(request.originalName(), usage, normalizedContentType);
        String objectKey = buildMediaObjectKey(usage, extension);
        StorageProperties.Video videoConfig = storageProperties.getVideo();
        StorageProperties.Audio audioConfig = storageProperties.getAudio();
        String bucketName = isVideoUsage(usage) ? videoConfig.getBucketName() : audioConfig.getBucketName();
        long uploadUrlTtlSeconds = isVideoUsage(usage) ? videoConfig.getUploadUrlTtlSeconds() : audioConfig.getUploadUrlTtlSeconds();
        var uploadUrl = storageClient.createPresignedUploadUrl(
                bucketName,
                objectKey,
                Duration.ofSeconds(uploadUrlTtlSeconds)
        );
        return new AdminCoverUploadResponse(
                usage,
                uploadUrl.method(),
                uploadUrl.url(),
                bucketName,
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
        if (!isExpectedMediaObjectKey(usage, objectKey)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Media objectKey is invalid");
        }
        String bucketName = isVideoUsage(usage)
                ? storageProperties.getVideo().getBucketName()
                : storageProperties.getAudio().getBucketName();
        StorageObjectStat objectStat = storageClient.statObject(bucketName, objectKey);
        validateObjectStat(usage, objectStat);

        String assetType = isVideoUsage(usage) ? VIDEO_ASSET_TYPE : AUDIO_ASSET_TYPE;
        FileAsset fileAsset = new FileAsset();
        fileAsset.setAssetType(assetType);
        fileAsset.setStorageProvider(storageProperties.getProvider());
        fileAsset.setBucketName(bucketName);
        fileAsset.setObjectKey(objectKey);
        fileAsset.setOriginalName(hasText(request.originalName()) ? request.originalName() : extractObjectName(objectKey));
        fileAsset.setContentType(normalizeContentType(objectStat.contentType()));
        fileAsset.setFileSize(objectStat.contentLength());
        fileAsset.setCreatedBy(currentAdminAccessor.getCurrentAdminId().orElse(0L));
        fileAsset.setDeleted(0);
        fileAssetMapper.insert(fileAsset);

        String mediaUrl = buildPublicFileUrl(fileAsset.getId());
        fileAsset.setUrl(mediaUrl);
        fileAssetMapper.updateById(fileAsset);

        return new AdminCoverAssetResponse(
                fileAsset.getId(),
                usage,
                mediaUrl,
                fileAsset.getBucketName(),
                fileAsset.getObjectKey(),
                fileAsset.getOriginalName(),
                fileAsset.getContentType(),
                fileAsset.getFileSize()
        );
    }

    private void validateUploadRequest(AdminCoverUploadRequest request) {
        String usage = normalizeUsage(request.usage());
        long maxFileSizeBytes = isVideoUsage(usage)
                ? storageProperties.getVideo().getMaxFileSizeBytes()
                : storageProperties.getAudio().getMaxFileSizeBytes();
        long fileSize = request.fileSize() == null ? 0 : request.fileSize();
        if (fileSize < 1 || fileSize > maxFileSizeBytes) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Media file size exceeds limit");
        }
        String contentType = normalizeContentType(request.contentType());
        if (!getAllowedContentTypes(usage).contains(contentType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Media content type is not supported");
        }
    }

    private void validateObjectStat(String usage, StorageObjectStat objectStat) {
        long maxFileSizeBytes = isVideoUsage(usage)
                ? storageProperties.getVideo().getMaxFileSizeBytes()
                : storageProperties.getAudio().getMaxFileSizeBytes();
        if (objectStat.contentLength() < 1 || objectStat.contentLength() > maxFileSizeBytes) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Uploaded media file size exceeds limit");
        }
        String contentType = normalizeContentType(objectStat.contentType());
        if (!getAllowedContentTypes(usage).contains(contentType)) {
            throw new BusinessException(ErrorCode.UNSUPPORTED_MEDIA_TYPE, "Uploaded media content type is not supported");
        }
    }

    private String normalizeUsage(String usage) {
        String normalizedUsage = usage == null ? "" : usage.trim().toLowerCase(Locale.ROOT);
        if (!MEDIA_USAGES.containsKey(normalizedUsage)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Unsupported media usage: " + usage);
        }
        return normalizedUsage;
    }

    private String buildMediaObjectKey(String usage, String extension) {
        String prefix = isVideoUsage(usage)
                ? storageProperties.getVideo().getObjectPrefix()
                : storageProperties.getAudio().getObjectPrefix();
        return prefix
                + "/"
                + MEDIA_USAGES.get(usage)
                + "/"
                + LocalDateTime.now().format(MEDIA_PATH_MONTH_FORMATTER)
                + "/"
                + UUID.randomUUID()
                + extension;
    }

    private boolean isExpectedMediaObjectKey(String usage, String objectKey) {
        String prefix = isVideoUsage(usage)
                ? storageProperties.getVideo().getObjectPrefix()
                : storageProperties.getAudio().getObjectPrefix();
        String expectedPrefix = prefix + "/" + MEDIA_USAGES.get(usage) + "/";
        return objectKey.startsWith(expectedPrefix);
    }

    private String resolveMediaExtension(String originalName, String usage, String contentType) {
        String normalizedContentType = normalizeContentType(contentType);
        Map<String, String> extensionMap = isVideoUsage(usage)
                ? VIDEO_CONTENT_TYPE_EXTENSIONS
                : AUDIO_CONTENT_TYPE_EXTENSIONS;
        String extension = extractExtension(originalName);
        if (!hasText(extension)) {
            return extensionMap.get(normalizedContentType);
        }
        if (".jpeg".equals(extension)) {
            extension = ".jpg";
        }
        String expectedExtension = extensionMap.get(normalizedContentType);
        if (!Objects.equals(extension, expectedExtension)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "Media file extension does not match content type");
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

    private boolean isVideoUsage(String usage) {
        return "course-video".equals(usage);
    }

    private List<String> getAllowedContentTypes(String usage) {
        return isVideoUsage(usage)
                ? storageProperties.getVideo().getAllowedContentTypes()
                : storageProperties.getAudio().getAllowedContentTypes();
    }

    private static Map<String, String> createMediaUsages() {
        Map<String, String> usages = new LinkedHashMap<>();
        usages.put("course-video", "course");
        usages.put("podcast-audio", "podcast");
        return usages;
    }
}
