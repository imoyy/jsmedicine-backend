package com.gugugaga.jsmedicine.module.interaction.feedback.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.FeedbackStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("feedbacks")
@EqualsAndHashCode(callSuper = true)
public class Feedback extends ManagedEntity {
    private Long userId;
    private Long studentId;
    private String feedbackType;
    private String content;
    private String contact;
    private FeedbackStatus status;
    private Long processedBy;
    private LocalDateTime processedAt;
    private String processNote;
}

