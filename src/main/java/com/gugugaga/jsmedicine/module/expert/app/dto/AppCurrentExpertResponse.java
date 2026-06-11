package com.gugugaga.jsmedicine.module.expert.app.dto;

public record AppCurrentExpertResponse(
        Boolean enabled,
        Long expertId,
        String realName,
        Boolean consultEnabled
) {
}
