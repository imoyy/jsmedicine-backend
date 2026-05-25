package com.gugugaga.jsmedicine.module.interaction.share.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("user_share_records")
@EqualsAndHashCode(callSuper = true)
public class UserShareRecord extends BaseEntity {
    private Long userId;
    private String resourceType;
    private Long resourceId;
    private String shareChannel;
}
