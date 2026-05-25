package com.gugugaga.jsmedicine.common.enums;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PermissionType {
    MENU(1),
    BUTTON(2),
    API(3);

    @EnumValue
    @JsonValue
    private final int value;
}
