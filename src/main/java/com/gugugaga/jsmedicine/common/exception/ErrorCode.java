package com.gugugaga.jsmedicine.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    BAD_REQUEST("BAD_REQUEST", "Invalid request"),
    UNAUTHORIZED("UNAUTHORIZED", "Unauthorized"),
    FORBIDDEN("FORBIDDEN", "Forbidden"),
    NOT_FOUND("NOT_FOUND", "Resource not found"),
    CONFLICT("CONFLICT", "Resource conflict"),
    INTERNAL_ERROR("INTERNAL_ERROR", "Internal server error");

    private final String code;
    private final String message;
}
