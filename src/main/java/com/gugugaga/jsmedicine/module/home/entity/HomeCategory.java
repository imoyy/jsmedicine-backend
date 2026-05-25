package com.gugugaga.jsmedicine.module.home.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("home_categories")
@EqualsAndHashCode(callSuper = true)
public class HomeCategory extends ManagedEntity {
    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private String iconUrl;
    private String description;
    private Integer sortOrder;
    private EnabledStatus status;
}
