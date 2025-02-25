package com.zipsoon.batch.score.model;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class EstateScore {
    private Long id;
    private Long estateSnapshotId;
    private Long scoreTypeId;
    private Double rawScore;
    private Double normalizedScore;
    private LocalDateTime createdAt;
}