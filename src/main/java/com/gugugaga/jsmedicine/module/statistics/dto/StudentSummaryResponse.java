package com.gugugaga.jsmedicine.module.statistics.dto;

public record StudentSummaryResponse(
        Long totalStudents,
        Long enabledStudents,
        Long approvedStudents,
        Long pendingCertifications,
        Long rejectedCertifications,
        Long linkedUsers
) {
}
