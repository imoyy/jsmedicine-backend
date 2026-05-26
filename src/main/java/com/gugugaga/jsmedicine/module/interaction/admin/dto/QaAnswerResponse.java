package com.gugugaga.jsmedicine.module.interaction.admin.dto;

import java.time.LocalDateTime;

public record QaAnswerResponse(
        Long id,
        Long questionId,
        Long adminId,
        Long expertId,
        String content,
        LocalDateTime answeredAt
) {
}
