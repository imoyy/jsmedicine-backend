package com.gugugaga.jsmedicine.common.response;

public record ValidationErrorItem(String field, String message, Object rejectedValue) {
}
