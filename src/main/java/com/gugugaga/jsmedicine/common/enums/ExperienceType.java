package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExperienceType {
    EDUCATION("education"),
    WORK("work"),
    ACHIEVEMENT("achievement");

    @EnumValue
    @JsonValue
    private final String value;
}
