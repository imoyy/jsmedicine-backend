package com.gugugaga.jsmedicine.module.expert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("expert_certification_category_relations")
public class ExpertCertificationCategoryRelation {
    private Long id;
    private Long certificationId;
    private Long categoryId;
    private LocalDateTime createdAt;
}
