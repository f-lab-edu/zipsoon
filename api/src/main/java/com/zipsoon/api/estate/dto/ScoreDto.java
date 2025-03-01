package com.zipsoon.api.estate.dto;

import lombok.Getter;

@Getter
public class ScoreDto {
    private Long scoreId;
    private Long scoreTypeId;
    private String scoreTypeName;
    private String description;
    private Double rawScore;
    private Double normalizedScore;
}