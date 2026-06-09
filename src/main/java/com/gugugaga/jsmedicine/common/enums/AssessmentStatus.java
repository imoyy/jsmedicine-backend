package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum AssessmentStatus {
    NOT_STARTED("not_started"),
    IN_PROGRESS("in_progress"),
    ENDED("ended"),
    CANCELLED("cancelled"),
    ARCHIVED("archived");

    @EnumValue
    @JsonValue
    private final String value;
}
