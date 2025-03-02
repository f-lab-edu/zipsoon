package com.zipsoon.batch.score.calculator;

import com.zipsoon.batch.normalize.normalizer.ScoreNormalizer;
import com.zipsoon.common.domain.Estate;
import org.springframework.lang.Nullable;

public interface ScoreCalculator {
    Long getScoreId();
    @Nullable ScoreNormalizer getNormalizer();
    double calculateRawScore(Estate estate);
}