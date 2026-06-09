package com.gugugaga.jsmedicine.module.statistics.dto;

import java.util.List;

public record AssessmentDashboardResponse(
        AssessmentDashboardOverviewResponse overview,
        AssessmentPaperStructureResponse paperStructure,
        List<AssessmentDistributionResponse> distribution,
        List<AssessmentTrendPointResponse> trend,
        List<AssessmentLatestEventResponse> latestEvents
) {
}
