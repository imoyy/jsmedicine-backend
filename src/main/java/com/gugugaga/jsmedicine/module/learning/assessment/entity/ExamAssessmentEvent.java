package com.gugugaga.jsmedicine.module.learning.assessment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import com.gugugaga.jsmedicine.common.enums.AssessmentEventType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("exam_assessment_events")
@EqualsAndHashCode(callSuper = true)
public class ExamAssessmentEvent extends BaseEntity {
    private Long assessmentId;
    private Long studentId;
    private AssessmentEventType eventType;
    private String requestId;
    private LocalDateTime eventTime;
    private String description;
    private String provinceCodeSnapshot;
    private String provinceNameSnapshot;
    private String cityCodeSnapshot;
    private String cityNameSnapshot;
    private String districtCodeSnapshot;
    private String districtNameSnapshot;
    private Long organizationIdSnapshot;
    private String organizationNameSnapshot;
}
