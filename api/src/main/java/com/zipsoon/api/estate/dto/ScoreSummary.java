package com.zipsoon.api.estate.dto;

import com.zipsoon.common.domain.EstateSnapshot;

import java.util.List;

public record ScoreSummary(
    Double total,
    List<TopFactor> topFactors
) {
    public static ScoreSummary fromEstateSnapshot(EstateSnapshot snapshot) {
        return null; // 구현 필요
    }

    public record TopFactor(
        Long id,
        String name,
        Double score
    ) {}
}
