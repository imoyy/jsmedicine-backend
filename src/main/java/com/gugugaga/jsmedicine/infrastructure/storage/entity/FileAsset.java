package com.gugugaga.jsmedicine.infrastructure.storage.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("file_assets")
@EqualsAndHashCode(callSuper = true)
public class FileAsset extends BaseEntity {
    private String assetType;
    private String storageProvider;
    private String bucketName;
    private String objectKey;
    private String originalName;
    private String contentType;
    private Long fileSize;
    private String url;
    private Long createdBy;

    @TableLogic
    private Integer deleted;
}
