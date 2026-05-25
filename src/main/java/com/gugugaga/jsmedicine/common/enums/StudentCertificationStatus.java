package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum StudentCertificationStatus {
    UNSUBMITTED(0),
    PENDING(1),
    APPROVED(2),
    REJECTED(3);

    @EnumValue
    @JsonValue
    private final int value;
}
