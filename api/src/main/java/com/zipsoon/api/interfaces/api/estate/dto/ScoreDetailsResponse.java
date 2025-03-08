package com.zipsoon.api.interfaces.api.estate.dto;

import java.util.List;

public record ScoreDetailsResponse(
    Double total,
    String description,
    List<ScoreFactorResponse> factors
) {
    public record ScoreFactorResponse(
        Long id,
        String name,
        String description,
        Double score
    ) {}
}
