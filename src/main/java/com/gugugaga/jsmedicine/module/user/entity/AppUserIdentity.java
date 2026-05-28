package com.gugugaga.jsmedicine.module.user.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.gugugaga.jsmedicine.common.entity.ManagedEntity;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityStatus;
import com.gugugaga.jsmedicine.common.enums.AppUserIdentityType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

@Data
@TableName("app_user_identities")
@EqualsAndHashCode(callSuper = true)
public class AppUserIdentity extends ManagedEntity {
    private Long userId;
    private AppUserIdentityType identityType;
    private AppUserIdentityStatus identityStatus;
    private Boolean isPrimary;
    private LocalDateTime activatedAt;
    private LocalDateTime deactivatedAt;
}
