package com.gugugaga.jsmedicine.module.learning.record.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("exam_records")
@EqualsAndHashCode(callSuper = true)
public class ExamRecord extends BaseEntity {
    private Long studentId;
    private Long paperId;
    private String sourceType;
    private Long sourceId;
    private BigDecimal score;
    private Integer passed;
    private LocalDateTime startedAt;
    private LocalDateTime submittedAt;
}

