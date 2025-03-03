package com.zipsoon.batch.domain.score;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ScoreType {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
}