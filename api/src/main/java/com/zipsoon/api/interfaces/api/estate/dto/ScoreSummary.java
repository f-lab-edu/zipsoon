package com.zipsoon.api.interfaces.api.estate.dto;

import java.util.List;

public record ScoreSummary(
    Double total,
    List<TopFactor> topFactors
) {
    public record TopFactor(
        Long id,
        String name,
        Double score
    ) {}
}
