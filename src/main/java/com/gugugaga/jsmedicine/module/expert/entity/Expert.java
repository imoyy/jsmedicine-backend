package com.gugugaga.jsmedicine.module.expert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;

@Data
@TableName("experts")
@EqualsAndHashCode(callSuper = true)
public class Expert extends ManagedEntity {
    private Long userId;
    private String realName;
    private Gender gender;
    private LocalDate birthDate;
    private String mobile;
    private String avatarUrl;
    private String coverUrl;
    private Long coverFileAssetId;
    private String title;
    private String organization;
    private Long organizationId;
    private String specialty;
    private Long practiceTypeId;
    private String introduction;
    private EnabledStatus status;
    private EnabledStatus consultEnabled;
    private String consultationNotice;
    private Integer sortOrder;
}
