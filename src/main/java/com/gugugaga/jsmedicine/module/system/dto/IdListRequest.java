package com.gugugaga.jsmedicine.module.system.dto;

import jakarta.validation.constraints.NotNull;

import java.util.List;

public record IdListRequest(
        @NotNull(message = "ids must not be null")
        List<Long> ids
) {
}
