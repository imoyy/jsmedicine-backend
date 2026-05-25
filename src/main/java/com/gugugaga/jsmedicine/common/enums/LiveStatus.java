package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum LiveStatus {
    NOT_STARTED(0),
    LIVE(1),
    ENDED(2),
    CANCELED(3);

    @EnumValue
    @JsonValue
    private final int value;
}
