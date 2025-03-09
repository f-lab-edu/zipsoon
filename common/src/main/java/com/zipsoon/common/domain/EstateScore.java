package com.zipsoon.common.domain;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

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
     * normalizedScore는 전처리 과정을 통해서만 생성됩니다.
     *
     * @param estateId 매물 ID
     * @param scoreTypeId 점수 유형 ID
     * @param rawScore 원시 점수
     * @return 생성된 매물 점수 객체
     */
    public static EstateScore of(Long estateId, Long scoreTypeId, Double rawScore) {
        if (estateId == null || scoreTypeId == null) {
            throw new IllegalArgumentException("매물 ID와 점수 유형 ID는 필수입니다");
        }
        
        return EstateScore.builder()
                .estateId(estateId)
                .scoreTypeId(scoreTypeId)
                .rawScore(rawScore)
                .createdAt(LocalDateTime.now())
                .build();
    }
    
    /**
     * 정규화된 점수를 설정합니다.
     * 
     * @param normalizedScore 정규화된 점수
     * @return 정규화된 점수가 설정된 새 EstateScore 객체
     */
    public EstateScore withNormalizedScore(Double normalizedScore) {
        if (normalizedScore != null && (normalizedScore < 0 || normalizedScore > 10)) {
            throw new IllegalArgumentException("정규화된 점수는 0에서 10 사이여야 합니다");
        }
        
        return EstateScore.builder()
                .id(this.id)
                .estateId(this.estateId)
                .scoreTypeId(this.scoreTypeId)
                .rawScore(this.rawScore)
                .normalizedScore(normalizedScore)
                .createdAt(this.createdAt)
                .build();
    }
    
    /**
     * 점수 목록의 평균을 계산합니다.
     * 
     * @param scores 점수 목록
     * @return 평균 점수
     */
    public static double calculateAverageScore(List<EstateScore> scores) {
        if (scores == null || scores.isEmpty()) {
            return 0.0;
        }
        
        return scores.stream()
                .filter(score -> score.getNormalizedScore() != null)
                .mapToDouble(EstateScore::getNormalizedScore)
                .average()
                .orElse(0.0);
    }
}