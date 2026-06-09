package com.gugugaga.jsmedicine.module.learning.assessment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.AssessmentStatus;
import com.gugugaga.jsmedicine.common.enums.AssessmentType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("exam_assessments")
@EqualsAndHashCode(callSuper = true)
public class ExamAssessment extends ManagedEntity {
    private String assessmentName;
    private Long paperId;
    private AssessmentType assessmentType;
    private AssessmentStatus status;
    private LocalDateTime startAt;
    private LocalDateTime endAt;
    private String provinceCode;
    private String cityCode;
    private String districtCode;
    private Long expectedStudentCount;
}
