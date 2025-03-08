package com.zipsoon.api.interfaces.api.estate.dto;

public record ScoreResponse(
    Long scoreId,
    Long scoreTypeId,
    String scoreTypeName,
    String description,
    Double rawScore,
    Double normalizedScore
) {}