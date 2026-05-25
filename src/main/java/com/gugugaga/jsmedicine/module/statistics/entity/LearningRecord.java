package com.gugugaga.jsmedicine.module.statistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("learning_records")
@EqualsAndHashCode(callSuper = true)
public class LearningRecord extends BaseEntity {
    private Long studentId;
    private String resourceType;
    private Long resourceId;
    private Integer studySeconds;
    private BigDecimal progressPercent;
    private Integer completed;
    private LocalDateTime completedAt;
    private LocalDateTime lastStudiedAt;
    private LocalDateTime updatedAt;
}
