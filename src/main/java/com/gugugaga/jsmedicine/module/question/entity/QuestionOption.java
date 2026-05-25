package com.gugugaga.jsmedicine.module.question.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("question_options")
@EqualsAndHashCode(callSuper = true)
public class QuestionOption extends BaseEntity {
    private Long questionId;
    private String optionKey;
    private String optionContent;
    private Integer correct;
    private Integer sortOrder;
}
