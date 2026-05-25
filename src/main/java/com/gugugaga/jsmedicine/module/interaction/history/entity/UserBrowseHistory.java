package com.gugugaga.jsmedicine.module.interaction.history.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("user_browse_histories")
@EqualsAndHashCode(callSuper = true)
public class UserBrowseHistory extends BaseEntity {
    private Long userId;
    private String resourceType;
    private Long resourceId;
    private String source;
    private Integer viewCount;
    private LocalDateTime viewedAt;
    private LocalDateTime updatedAt;
}
