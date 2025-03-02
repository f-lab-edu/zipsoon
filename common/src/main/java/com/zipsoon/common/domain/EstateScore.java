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
public class EstateScore {
    private Long id;
    private Long estateId;
    private Long scoreTypeId;
    private Double rawScore;
    private Double normalizedScore;
    private LocalDateTime createdAt;
}