package com.gugugaga.jsmedicine.module.qa.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.QaStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("qa_questions")
@EqualsAndHashCode(callSuper = true)
public class QaQuestion extends ManagedEntity {
    private Long studentId;
    private Long userId;
    private String title;
    private String content;
    private QaStatus status;
}
