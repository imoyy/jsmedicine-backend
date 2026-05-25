package com.gugugaga.jsmedicine.module.learning.question.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("exam_paper_questions")
@EqualsAndHashCode(callSuper = true)
public class ExamPaperQuestion extends BaseEntity {
    private Long paperId;
    private Long questionId;
    private BigDecimal score;
    private Integer sortOrder;
}

