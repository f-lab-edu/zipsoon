package com.zipsoon.batch.score.calculator;

import com.zipsoon.batch.normalize.normalizer.ScoreNormalizer;
import com.zipsoon.common.domain.EstateSnapshot;
import org.springframework.lang.Nullable;

public interface ScoreCalculator {
    Long getScoreId();
    @Nullable ScoreNormalizer getNormalizer();
    double calculateRawScore(EstateSnapshot estate);
}