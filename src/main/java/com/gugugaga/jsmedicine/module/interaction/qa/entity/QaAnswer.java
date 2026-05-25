package com.gugugaga.jsmedicine.module.interaction.qa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("qa_answers")
@EqualsAndHashCode(callSuper = true)
public class QaAnswer extends BaseEntity {
    private Long questionId;
    private Long adminId;
    private Long expertId;
    private String content;
    private LocalDateTime answeredAt;

    @TableLogic
    private Integer deleted;
}

