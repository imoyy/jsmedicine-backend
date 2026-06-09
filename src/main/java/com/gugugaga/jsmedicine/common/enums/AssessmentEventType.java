package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssessmentEventType {
    ENTER("enter"),
    SUBMIT("submit"),
    TIMEOUT("timeout"),
    FORCED_SUBMIT("forced_submit"),
    DISCONNECT("disconnect"),
    RECONNECT("reconnect"),
    ABNORMAL_EXIT("abnormal_exit");

    @EnumValue
    @JsonValue
    private final String value;
}
