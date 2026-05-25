package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FeedbackStatus {
    PENDING(0),
    PROCESSED(1);

    @EnumValue
    @JsonValue
    private final int value;
}
