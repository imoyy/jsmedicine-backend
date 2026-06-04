package com.gugugaga.jsmedicine.module.content.home.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("home_contents")
@EqualsAndHashCode(callSuper = true)
public class HomeContent extends ManagedEntity {
    private Long categoryId;
    private String contentType;
    private Long targetId;
    private String title;
    private String coverUrl;
    private Long coverFileAssetId;
    private String linkUrl;
    private Integer sortOrder;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private EnabledStatus status;
}

