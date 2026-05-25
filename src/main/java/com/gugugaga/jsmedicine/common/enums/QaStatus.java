package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QaStatus {
    PENDING(0),
    ANSWERED(1),
    CLOSED(2);

    @EnumValue
    @JsonValue
    private final int value;
}
