package com.gugugaga.jsmedicine.module.expert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("expert_categories")
@EqualsAndHashCode(callSuper = true)
public class ExpertCategory extends ManagedEntity {
    private Long parentId;
    private String categoryName;
    private Integer sortOrder;
    private EnabledStatus status;
}
