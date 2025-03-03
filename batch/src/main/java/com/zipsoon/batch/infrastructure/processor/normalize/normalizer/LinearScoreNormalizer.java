package com.zipsoon.batch.infrastructure.processor.normalize.normalizer;

import com.zipsoon.batch.application.service.normalize.normalizer.ScoreNormalizer;

import java.util.List;

public class LinearScoreNormalizer implements ScoreNormalizer {
    private static final double BASE_SCORE = 1.0; // 최소 보장 점수
    private static final double MAX_SCORE = 10.0;

    @Override
    public double normalize(double rawScore, List<Double> scores) {
        double min = scores.stream().min(Double::compareTo).orElse(0.0);
        double max = scores.stream().max(Double::compareTo).orElse(10.0);

        if (min == max) {
            return 5.0;
        }

        double range = MAX_SCORE - BASE_SCORE;

        // 수정된 정규화 공식: BASE_SCORE + (value-min)/(max-min) * (MAX_SCORE-BASE_SCORE)
        double normalized = BASE_SCORE + ((rawScore - min) / (max - min)) * range;

        // 최소값은 BASE_SCORE, 최대값은 MAX_SCORE로 제한
        return Math.min(Math.max(normalized, BASE_SCORE), MAX_SCORE);
    }
}