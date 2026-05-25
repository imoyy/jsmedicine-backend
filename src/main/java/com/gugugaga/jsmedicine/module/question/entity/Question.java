package com.gugugaga.jsmedicine.module.question.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.QuestionType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;

@Data
@TableName("questions")
@EqualsAndHashCode(callSuper = true)
public class Question extends ManagedEntity {
    private Long categoryId;
    private QuestionType questionType;
    private String title;
    private String analysis;
    private Integer difficulty;
    private BigDecimal score;
    private EnabledStatus status;
}
