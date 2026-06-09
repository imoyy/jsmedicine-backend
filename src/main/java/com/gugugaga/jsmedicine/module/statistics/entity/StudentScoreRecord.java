package com.gugugaga.jsmedicine.module.statistics.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("student_score_records")
@EqualsAndHashCode(callSuper = true)
public class StudentScoreRecord extends ManagedEntity {
    private Long studentId;
    private String theoryTrainingStatus;
    private String clinicalPracticeStatus;
    private String practicalAssessmentStatus;
    private String theoryAssessmentStatus;
    private String onlineTrainingStatus;
}
