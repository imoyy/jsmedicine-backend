package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum QuestionType {
    SINGLE_CHOICE(1),
    MULTIPLE_CHOICE(2),
    TRUE_FALSE(3),
    SHORT_ANSWER(4);

    @EnumValue
    @JsonValue
    private final int value;
}
