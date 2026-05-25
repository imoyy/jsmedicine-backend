package com.gugugaga.jsmedicine.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("app_users")
@EqualsAndHashCode(callSuper = true)
public class AppUser extends ManagedEntity {
    private String username;
    private String mobile;
    private String email;
    private String nickname;
    private String avatarUrl;
    private Gender gender;
    private EnabledStatus status;
    private LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;
}

