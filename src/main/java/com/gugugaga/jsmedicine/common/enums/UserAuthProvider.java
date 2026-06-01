package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserAuthProvider {
    WECHAT_MINIAPP("wechat_miniapp"),
    WECHAT_WEB("wechat_web"),
    MOBILE_SMS("mobile_sms");

    @EnumValue
    @JsonValue
    private final String value;
}
