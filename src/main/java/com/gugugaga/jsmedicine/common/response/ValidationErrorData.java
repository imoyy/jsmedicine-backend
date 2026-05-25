package com.gugugaga.jsmedicine.common.response;

import java.util.List;

public record ValidationErrorData(List<ValidationErrorItem> errors) {
}
