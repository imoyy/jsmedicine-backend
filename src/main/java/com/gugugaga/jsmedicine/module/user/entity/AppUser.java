package com.gugugaga.jsmedicine.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.common.enums.UserAuthProvider;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("app_users")
@EqualsAndHashCode(callSuper = true)
public class AppUser extends ManagedEntity {
    private String username;
    private String passwordHash;
    private String mobile;
    private String email;
    private String nickname;
    private String profileSignature;
    private String avatarUrl;
    private UserAuthProvider authProvider;
    private String wechatOpenId;
    private String wechatWebOpenId;
    private String wechatUnionId;
    private Gender gender;
    private EnabledStatus status;
    private LocalDateTime registeredAt;
    private LocalDateTime lastLoginAt;
    private String lastLoginIp;
    private Boolean profileCompleted;
    private LocalDateTime passwordUpdatedAt;
}

