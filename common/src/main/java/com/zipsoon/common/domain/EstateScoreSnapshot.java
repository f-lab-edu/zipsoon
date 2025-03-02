package com.zipsoon.common.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder(access = AccessLevel.PUBLIC)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
public class EstateScoreSnapshot {
    private Long id;
    private Long estateSnapshotId;
    private Long scoreTypeId;
    private Double rawScore;
    private Double normalizedScore;
    private LocalDateTime createdAt;
}