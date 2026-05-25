package com.gugugaga.jsmedicine.module.learning.question.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("exam_papers")
@EqualsAndHashCode(callSuper = true)
public class ExamPaper extends ManagedEntity {
    private String paperName;
    private String description;
    private BigDecimal totalScore;
    private BigDecimal passScore;
    private Integer durationMinutes;
    private EnabledStatus status;
}

