package com.gugugaga.jsmedicine.module.user.dto;

public record AdminStudentImportFailureResponse(
        Integer rowNumber,
        String studentNo,
        String realName,
        String errorMessage
) {
}
