package com.zipsoon.api.interfaces.api.estate.dto;

import java.util.List;

public record ScoreDetails(
    Double total,
    String description,
    List<ScoreFactor> factors
) {
    public record ScoreFactor(
        Long id,
        String name,
        String description,
        Double score
    ) {}
}
