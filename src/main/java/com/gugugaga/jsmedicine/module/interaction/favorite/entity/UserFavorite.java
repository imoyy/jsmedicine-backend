package com.gugugaga.jsmedicine.module.interaction.favorite.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("user_favorites")
@EqualsAndHashCode(callSuper = true)
public class UserFavorite extends BaseEntity {
    private Long userId;
    private String resourceType;
    private Long resourceId;
}
