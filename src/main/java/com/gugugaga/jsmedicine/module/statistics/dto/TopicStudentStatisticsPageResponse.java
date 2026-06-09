package com.gugugaga.jsmedicine.module.statistics.dto;

import java.util.List;

public record TopicStudentStatisticsPageResponse(
        TopicStudentStatisticsSummaryResponse summary,
        List<TopicStudentStatisticsRecordResponse> records,
        long total,
        long page,
        long size
) {
}
