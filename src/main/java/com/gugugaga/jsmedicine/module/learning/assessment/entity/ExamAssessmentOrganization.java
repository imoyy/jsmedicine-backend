package com.gugugaga.jsmedicine.module.learning.assessment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("exam_assessment_organizations")
@EqualsAndHashCode(callSuper = true)
public class ExamAssessmentOrganization extends BaseEntity {
    private Long assessmentId;
    private Long organizationId;
}
