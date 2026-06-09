package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExamRecordStatus {
    IN_PROGRESS("in_progress"),
    SUBMITTED("submitted"),
    FORCED_SUBMITTED("forced_submitted"),
    TIMED_OUT("timed_out");

    @EnumValue
    @JsonValue
    private final String value;
}
