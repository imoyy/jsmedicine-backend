package com.gugugaga.jsmedicine.module.learning.admin.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record BookCategoryBookBindingRequest(
        @NotNull(message = "bookIds must not be null")
        List<Long> bookIds
) {
}
