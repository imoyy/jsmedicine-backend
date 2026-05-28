package com.gugugaga.jsmedicine.module.user.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import com.gugugaga.jsmedicine.common.enums.Gender;
import com.gugugaga.jsmedicine.common.enums.UserAuthProvider;

import java.time.LocalDateTime;

public record AdminUserResponse(
        Long id,
        String username,
        String mobile,
        String email,
        String nickname,
        String profileSignature,
        String avatarUrl,
        UserAuthProvider authProvider,
        String wechatOpenId,
        String wechatUnionId,
        Gender gender,
        EnabledStatus status,
        LocalDateTime registeredAt,
        LocalDateTime lastLoginAt,
        String lastLoginIp,
        Boolean profileCompleted
) {
}
