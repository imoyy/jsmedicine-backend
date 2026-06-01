package com.gugugaga.jsmedicine.infrastructure.storage.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.gugugaga.jsmedicine.infrastructure.storage.StorageProperties;
import com.gugugaga.jsmedicine.infrastructure.storage.entity.FileAsset;
import com.gugugaga.jsmedicine.infrastructure.storage.mapper.FileAssetMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AppUserAvatarUrlResolver {

    private static final String IMAGE_ASSET_TYPE = "image";
    private static final String PUBLIC_FILE_URL_TEMPLATE = "/api/v1/files/%d/content";

    private final FileAssetMapper fileAssetMapper;
    private final StorageProperties storageProperties;

    public AppUserAvatarUrlResolver(FileAssetMapper fileAssetMapper, StorageProperties storageProperties) {
        this.fileAssetMapper = fileAssetMapper;
        this.storageProperties = storageProperties;
    }

    public String resolve(Long userId, String currentAvatarUrl) {
        String stableUrl = normalizeStableUrl(currentAvatarUrl);
        if (stableUrl != null) {
            return stableUrl;
        }
        if (hasText(currentAvatarUrl)) {
            FileAsset matchedAsset = fileAssetMapper.selectOne(new LambdaQueryWrapper<FileAsset>()
                    .eq(FileAsset::getDeleted, 0)
                    .eq(FileAsset::getAssetType, IMAGE_ASSET_TYPE)
                    .eq(FileAsset::getUrl, currentAvatarUrl.trim())
                    .last("LIMIT 1"));
            if (isPublicAvatarAsset(matchedAsset)) {
                return buildPublicFileUrl(matchedAsset.getId());
            }
        }
        if (userId == null) {
            return null;
        }
        FileAsset latestAvatar = fileAssetMapper.selectOne(new LambdaQueryWrapper<FileAsset>()
                .eq(FileAsset::getDeleted, 0)
                .eq(FileAsset::getAssetType, IMAGE_ASSET_TYPE)
                .likeRight(FileAsset::getObjectKey, buildAvatarObjectPrefix(userId))
                .orderByDesc(FileAsset::getId)
                .last("LIMIT 1"));
        if (isPublicAvatarAsset(latestAvatar)) {
            return buildPublicFileUrl(latestAvatar.getId());
        }
        return null;
    }

    private String normalizeStableUrl(String currentAvatarUrl) {
        if (!hasText(currentAvatarUrl)) {
            return null;
        }
        String trimmedUrl = currentAvatarUrl.trim();
        if (trimmedUrl.matches(".*/api/v1/files/\\d+/content/?$")) {
            int pathIndex = trimmedUrl.indexOf("/api/v1/files/");
            return pathIndex >= 0 ? trimmedUrl.substring(pathIndex) : trimmedUrl;
        }
        return null;
    }

    private boolean isPublicAvatarAsset(FileAsset fileAsset) {
        return fileAsset != null
                && Objects.equals(fileAsset.getDeleted(), 0)
                && IMAGE_ASSET_TYPE.equalsIgnoreCase(fileAsset.getAssetType())
                && hasText(fileAsset.getObjectKey())
                && fileAsset.getObjectKey().startsWith(storageProperties.getAvatar().getObjectPrefix() + "/")
                && fileAsset.getObjectKey().contains("/avatars/");
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
