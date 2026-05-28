package com.gugugaga.jsmedicine.module.auth.app.dto;

public record AppWechatLoginResponse(
        Boolean registered,
        Boolean needBindMobile,
        String bindToken,
        String tokenType,
        String accessToken,
        Long expiresIn,
        AppLoginResponse.UserProfile user
) {
}
