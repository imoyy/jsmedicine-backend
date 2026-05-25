package com.gugugaga.jsmedicine.module.expert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@TableName("expert_experiences")
@EqualsAndHashCode(callSuper = true)
public class ExpertExperience extends ManagedEntity {
    private Long expertId;
    private String experienceType;
    private String title;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private Integer sortOrder;
}
