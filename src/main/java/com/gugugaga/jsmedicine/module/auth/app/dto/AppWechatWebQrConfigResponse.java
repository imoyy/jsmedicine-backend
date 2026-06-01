package com.gugugaga.jsmedicine.module.auth.app.dto;

public record AppWechatWebQrConfigResponse(
        String appId,
        String redirectUri,
        String scope,
        String state,
        Long expiresIn
) {
}
