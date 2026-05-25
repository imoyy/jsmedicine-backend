package com.gugugaga.jsmedicine.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_roles")
@EqualsAndHashCode(callSuper = true)
public class SysRole extends ManagedEntity {
    private String roleCode;
    private String roleName;
    private String description;
    private EnabledStatus status;
    private Integer sortOrder;
}
