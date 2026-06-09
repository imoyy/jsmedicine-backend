package com.gugugaga.jsmedicine.module.learning.assessment.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("exam_assessment_students")
@EqualsAndHashCode(callSuper = true)
public class ExamAssessmentStudent extends BaseEntity {
    private Long assessmentId;
    private Long studentId;
    private String assignSource;
    private String studentNameSnapshot;
    private String mobileSnapshot;
    private String maskedIdCardNoSnapshot;
    private String provinceCodeSnapshot;
    private String provinceNameSnapshot;
    private String cityCodeSnapshot;
    private String cityNameSnapshot;
    private String districtCodeSnapshot;
    private String districtNameSnapshot;
    private Long organizationIdSnapshot;
    private String organizationNameSnapshot;
}
