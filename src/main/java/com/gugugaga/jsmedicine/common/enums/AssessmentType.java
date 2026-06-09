package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssessmentType {
    FORMAL("formal"),
    MAKEUP("makeup"),
    MOCK("mock");

    @EnumValue
    @JsonValue
    private final String value;
}
