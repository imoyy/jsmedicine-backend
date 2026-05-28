package com.gugugaga.jsmedicine.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("organizations")
@EqualsAndHashCode(callSuper = true)
public class Organization extends ManagedEntity {
    private String orgCode;
    private String orgName;
    private String orgType;
    private String provinceCode;
    private String cityCode;
    private String districtCode;
    private String address;
    private EnabledStatus status;
    private Integer sortOrder;
}
