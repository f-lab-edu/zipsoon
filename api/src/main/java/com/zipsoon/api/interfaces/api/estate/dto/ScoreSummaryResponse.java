package com.zipsoon.api.interfaces.api.estate.dto;

import java.util.List;

public record ScoreSummaryResponse(
    Double total,
    List<TopFactorResponse> topFactors
) {
    public record TopFactorResponse(
        Long id,
        String name,
        Double score
    ) {}
}
