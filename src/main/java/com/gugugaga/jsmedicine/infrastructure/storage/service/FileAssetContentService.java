package com.gugugaga.jsmedicine.infrastructure.storage.service;

import com.gugugaga.jsmedicine.common.exception.BusinessException;
import com.gugugaga.jsmedicine.common.exception.ErrorCode;
import com.gugugaga.jsmedicine.infrastructure.storage.StorageClient;
import com.gugugaga.jsmedicine.infrastructure.storage.StorageObjectStream;
import com.gugugaga.jsmedicine.infrastructure.storage.StorageProperties;
import com.gugugaga.jsmedicine.infrastructure.storage.entity.FileAsset;
import com.gugugaga.jsmedicine.infrastructure.storage.mapper.FileAssetMapper;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FileAssetContentService {

    private final FileAssetMapper fileAssetMapper;
    private final StorageClient storageClient;
    private final StorageProperties storageProperties;

    public FileAssetContentService(
            FileAssetMapper fileAssetMapper,
            StorageClient storageClient,
            StorageProperties storageProperties
    ) {
        this.fileAssetMapper = fileAssetMapper;
        this.storageClient = storageClient;
        this.storageProperties = storageProperties;
    }

    public StorageObjectStream loadPublicContent(Long id) {
        FileAsset fileAsset = fileAssetMapper.selectById(id);
        if (fileAsset == null || !Objects.equals(fileAsset.getDeleted(), 0) || !isPublicAvatarAsset(fileAsset)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "File asset does not exist");
        }
        return storageClient.getObject(fileAsset.getBucketName(), fileAsset.getObjectKey());
    }

    private boolean isPublicAvatarAsset(FileAsset fileAsset) {
        return "image".equalsIgnoreCase(fileAsset.getAssetType())
                && fileAsset.getObjectKey() != null
                && fileAsset.getObjectKey().startsWith(storageProperties.getAvatar().getObjectPrefix() + "/")
                && fileAsset.getObjectKey().contains("/avatars/");
    }
}
