package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Difficulty {
    EASY(1),
    MEDIUM(2),
    HARD(3);

    @EnumValue
    @JsonValue
    private final int value;
}
