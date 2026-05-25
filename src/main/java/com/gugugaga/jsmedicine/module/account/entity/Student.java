package com.gugugaga.jsmedicine.module.account.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
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
    private String mobile;
    private String idCardNo;
    private String province;
    private String city;
    private String district;
    private String organization;
    private String positionTitle;
    private EnabledStatus status;
    private LocalDateTime enrolledAt;
}
