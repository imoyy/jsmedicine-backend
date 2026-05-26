package com.gugugaga.jsmedicine.module.learning.admin.dto;

public record QuestionOptionResponse(
        Long id,
        Long questionId,
        String optionKey,
        String optionContent,
        Integer correct,
        Integer sortOrder
) {
}
