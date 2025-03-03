package com.zipsoon.batch.domain.score.value;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 매물 점수 값을 표현하는 값 객체
 * 점수 타입과 원시 점수 및 정규화된 점수 정보 포함
 */
public record ScoreValue(String scoreType, double rawScore, Double normalizedScore) {

    public ScoreValue {
        if (rawScore < 0) {
            throw new IllegalArgumentException("Raw score cannot be negative");
        }
        if (normalizedScore != null && (normalizedScore < 0 || normalizedScore > 10)) {
            throw new IllegalArgumentException("Normalized score must be between 0 and 10");
        }
    }
    
    /**
     * 원시 점수만으로 ScoreValue 생성 (정규화 점수 없음)
     */
    public static ScoreValue ofRawScore(String scoreType, double rawScore) {
        return new ScoreValue(scoreType, rawScore, null);
    }
    
    /**
     * 정규화된 점수를 소수점 2자리로 반올림하여 반환
     */
    public Double getNormalizedScoreRounded() {
        if (normalizedScore == null) {
            return null;
        }
        return BigDecimal.valueOf(normalizedScore)
            .setScale(2, RoundingMode.HALF_UP)
            .doubleValue();
    }
    
    /**
     * 정규화된 점수가 설정되었는지 확인
     */
    public boolean isNormalized() {
        return normalizedScore != null;
    }
    
    /**
     * 동일한 원시 점수와 유형으로 정규화된 점수를 설정한 새 객체 반환
     */
    public ScoreValue withNormalizedScore(double normalizedScore) {
        return new ScoreValue(this.scoreType, this.rawScore, normalizedScore);
    }
}