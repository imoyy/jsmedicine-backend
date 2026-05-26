package com.gugugaga.jsmedicine.module.learning.app.dto;

public record AppExamQuestionOptionResponse(
        Long id,
        String optionKey,
        String optionContent,
        Integer sortOrder
) {
}
