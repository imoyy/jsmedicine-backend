package com.gugugaga.jsmedicine.module.expert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("expert_category_relations")
@EqualsAndHashCode(callSuper = true)
public class ExpertCategoryRelation extends BaseEntity {
    private Long expertId;
    private Long categoryId;
}
