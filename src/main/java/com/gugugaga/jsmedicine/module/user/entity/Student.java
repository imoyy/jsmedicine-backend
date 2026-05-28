package com.gugugaga.jsmedicine.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.common.enums.StudentCertificationStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("students")
@EqualsAndHashCode(callSuper = true)
public class Student extends ManagedEntity {
    private Long userId;
    private String studentNo;
    private String realName;
    private Gender gender;
    private Integer age;
    private String educationLevel;
    private String mobile;
    private String idCardNo;
    private String province;
    private String provinceCode;
    private String city;
    private String cityCode;
    private String district;
    private String districtCode;
    private String organization;
    private Long organizationId;
    private String positionTitle;
    private Long practiceTypeId;
    private EnabledStatus status;
    private StudentCertificationStatus certificationStatus;
    private LocalDateTime certificationSubmittedAt;
    private LocalDateTime certificationReviewedAt;
    private Long certificationReviewedBy;
    private String rejectReason;
    private String certificationMaterials;
    private LocalDateTime enrolledAt;
}

