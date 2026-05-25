package com.gugugaga.jsmedicine.module.knowledge.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("knowledge_categories")
@EqualsAndHashCode(callSuper = true)
public class KnowledgeCategory extends ManagedEntity {
    private Long parentId;
    private String categoryName;
    private String categoryCode;
    private String description;
    private Integer sortOrder;
    private EnabledStatus status;
}
