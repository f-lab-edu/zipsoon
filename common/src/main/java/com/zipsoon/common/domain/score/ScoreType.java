package com.zipsoon.common.domain.score;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 매물 점수 유형 정보
 */
@Getter
@Builder
public class ScoreType {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
}