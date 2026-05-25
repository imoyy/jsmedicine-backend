package com.gugugaga.jsmedicine.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@TableName("sys_admin_roles")
@EqualsAndHashCode(callSuper = true)
public class SysAdminRole extends BaseEntity {
    private Long adminId;
    private Long roleId;
}
