package com.zipsoon.api.estate.dto;

import com.zipsoon.common.domain.EstateSnapshot;

import java.util.List;

public record ScoreDetails(
    Double total,
    String description,
    List<ScoreFactor> factors
) {
    public static ScoreDetails fromEstateSnapshot(EstateSnapshot snapshot) {
        return null;
    }

    public record ScoreFactor(
        Long id,
        String name,
        String description,
        Double score
    ) {}
}
