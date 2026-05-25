package com.gugugaga.jsmedicine.module.learning.record.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("exam_record_answers")
@EqualsAndHashCode(callSuper = true)
public class ExamRecordAnswer extends BaseEntity {
    private Long examRecordId;
    private Long questionId;
    private String answerContent;
    private BigDecimal score;
    private Integer correct;
}

