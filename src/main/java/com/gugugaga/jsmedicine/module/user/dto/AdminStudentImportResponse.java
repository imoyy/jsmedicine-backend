package com.gugugaga.jsmedicine.module.user.dto;

import java.util.List;

public record AdminStudentImportResponse(
        Integer totalRows,
        Integer successCount,
        Integer failureCount,
        List<AdminStudentImportFailureResponse> failures
) {
}
