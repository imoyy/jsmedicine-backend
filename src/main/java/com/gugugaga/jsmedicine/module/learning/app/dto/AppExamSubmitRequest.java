package com.gugugaga.jsmedicine.module.learning.app.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AppExamSubmitRequest(
        String sourceType,
        Long sourceId,

        @NotEmpty(message = "answers must not be empty")
        @Valid
        List<Answer> answers
) {
    public record Answer(
            @NotNull(message = "questionId must not be null")
            Long questionId,

            @Size(max = 4000, message = "answerContent length must be less than 4000")
            String answerContent
    ) {
    }
}
