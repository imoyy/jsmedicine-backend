package com.gugugaga.jsmedicine.module.expert.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("experts")
@EqualsAndHashCode(callSuper = true)
public class Expert extends ManagedEntity {
    private Long userId;
    private String realName;
    private String avatarUrl;
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
