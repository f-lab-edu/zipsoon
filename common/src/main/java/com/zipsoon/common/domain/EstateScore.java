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
    
    /**
     * 매물 점수 객체를 생성합니다.
     *
     * @param estateId 매물 ID
     * @param scoreTypeId 점수 유형 ID
     * @param rawScore 원시 점수
     * @param normalizedScore 정규화된 점수
     * @return 생성된 매물 점수 객체
     */
    public static EstateScore of(Long estateId, Long scoreTypeId, Double rawScore, Double normalizedScore) {
        return EstateScore.builder()
                .estateId(estateId)
                .scoreTypeId(scoreTypeId)
                .rawScore(rawScore)
                .normalizedScore(normalizedScore)
                .createdAt(LocalDateTime.now())
                .build();
    }
}