package com.gugugaga.jsmedicine.module.system.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("sys_admins")
@EqualsAndHashCode(callSuper = true)
public class SysAdmin extends ManagedEntity {
    private String username;
    private String passwordHash;
    private String realName;
    private String mobile;
    private String email;
    private String avatarUrl;
    private EnabledStatus status;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
}
