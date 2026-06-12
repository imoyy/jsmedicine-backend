package com.gugugaga.jsmedicine.module.expert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.ExpertCertificationStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("expert_certifications")
@EqualsAndHashCode(callSuper = true)
public class ExpertCertification extends ManagedEntity {
    private Long userId;
    private String realName;
    private Gender gender;
    private LocalDate birthDate;
    private String mobile;
    private String title;
    private String organization;
    private Long organizationId;
    private Long practiceTypeId;
    private String specialty;
    private String introduction;
    private String consultationNotice;
    private ExpertCertificationStatus certificationStatus;
    private LocalDateTime certificationSubmittedAt;
    private LocalDateTime certificationReviewedAt;
    private Long certificationReviewedBy;
    private String rejectReason;
}
