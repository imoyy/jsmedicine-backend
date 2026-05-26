package com.gugugaga.jsmedicine.module.system.dto;

import com.gugugaga.jsmedicine.common.enums.EnabledStatus;
import jakarta.validation.constraints.NotNull;

public record StatusUpdateRequest(
        @NotNull(message = "status must not be null")
        EnabledStatus status
) {
}
