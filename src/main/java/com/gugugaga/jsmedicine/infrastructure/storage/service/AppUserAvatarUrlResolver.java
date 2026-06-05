package com.gugugaga.jsmedicine.infrastructure.storage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.infrastructure.storage.StorageClient;
import com.gugugaga.jsmedicine.infrastructure.storage.StorageProperties;
import com.gugugaga.jsmedicine.infrastructure.storage.entity.FileAsset;
import com.gugugaga.jsmedicine.infrastructure.storage.mapper.FileAssetMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class AppUserAvatarUrlResolver {

    private static final String IMAGE_ASSET_TYPE = "image";
    private static final String PUBLIC_FILE_URL_TEMPLATE = "/api/v1/files/%d/content";
    private static final String DEFAULT_AVATAR_URL = "/images/default-avatar.svg";
    private static final Pattern PUBLIC_FILE_URL_PATTERN = Pattern.compile(".*/api/v1/files/(\\d+)/content/?$");

    private final FileAssetMapper fileAssetMapper;
    private final StorageClient storageClient;
    private final StorageProperties storageProperties;

    public AppUserAvatarUrlResolver(
            FileAssetMapper fileAssetMapper,
            StorageClient storageClient,
            StorageProperties storageProperties
    ) {
        this.fileAssetMapper = fileAssetMapper;
        this.storageClient = storageClient;
        this.storageProperties = storageProperties;
    }

    public String resolve(Long userId, String currentAvatarUrl) {
        FileAsset stableAsset = findStableAvatarAsset(currentAvatarUrl);
        if (isReadablePublicAvatarAsset(stableAsset)) {
            return buildPublicFileUrl(stableAsset.getId());
        }
        if (hasText(currentAvatarUrl)) {
            FileAsset matchedAsset = fileAssetMapper.selectOne(new LambdaQueryWrapper<FileAsset>()
                    .eq(FileAsset::getDeleted, 0)
                    .eq(FileAsset::getAssetType, IMAGE_ASSET_TYPE)
                    .eq(FileAsset::getUrl, currentAvatarUrl.trim())
                    .last("LIMIT 1"));
            if (isReadablePublicAvatarAsset(matchedAsset)) {
                return buildPublicFileUrl(matchedAsset.getId());
            }
        }
        if (userId == null) {
            return DEFAULT_AVATAR_URL;
        }
        FileAsset latestAvatar = fileAssetMapper.selectOne(new LambdaQueryWrapper<FileAsset>()
                .eq(FileAsset::getDeleted, 0)
                .eq(FileAsset::getAssetType, IMAGE_ASSET_TYPE)
                .likeRight(FileAsset::getObjectKey, buildAvatarObjectPrefix(userId))
                .orderByDesc(FileAsset::getId)
                .last("LIMIT 1"));
        if (isReadablePublicAvatarAsset(latestAvatar)) {
            return buildPublicFileUrl(latestAvatar.getId());
        }
        return DEFAULT_AVATAR_URL;
    }

    private FileAsset findStableAvatarAsset(String currentAvatarUrl) {
        if (!hasText(currentAvatarUrl)) {
            return null;
        }
        String trimmedUrl = currentAvatarUrl.trim();
        Matcher matcher = PUBLIC_FILE_URL_PATTERN.matcher(trimmedUrl);
        if (!matcher.matches()) {
            return null;
        }
        return fileAssetMapper.selectById(Long.parseLong(matcher.group(1)));
    }

    private boolean isReadablePublicAvatarAsset(FileAsset fileAsset) {
        return isPublicAvatarAsset(fileAsset) && objectExists(fileAsset);
    }

    private boolean isPublicAvatarAsset(FileAsset fileAsset) {
        return fileAsset != null
                && Objects.equals(fileAsset.getDeleted(), 0)
                && IMAGE_ASSET_TYPE.equalsIgnoreCase(fileAsset.getAssetType())
                && hasText(fileAsset.getObjectKey())
                && fileAsset.getObjectKey().startsWith(storageProperties.getAvatar().getObjectPrefix() + "/")
                && fileAsset.getObjectKey().contains("/avatars/");
    }

    private boolean objectExists(FileAsset fileAsset) {
        try {
            storageClient.statObject(fileAsset.getBucketName(), fileAsset.getObjectKey());
            return true;
        } catch (BusinessException exception) {
            if (exception.getErrorCode() == ErrorCode.NOT_FOUND) {
                return false;
            }
            throw exception;
        }
    }

    private String buildAvatarObjectPrefix(Long userId) {
        return storageProperties.getAvatar().getObjectPrefix() + "/" + userId + "/avatars/";
    }

    private String buildPublicFileUrl(Long fileAssetId) {
        return PUBLIC_FILE_URL_TEMPLATE.formatted(fileAssetId);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
