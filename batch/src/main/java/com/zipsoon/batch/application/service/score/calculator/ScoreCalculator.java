package com.zipsoon.batch.application.service.score.calculator;

import com.zipsoon.batch.application.service.normalize.normalizer.ScoreNormalizer;
import com.zipsoon.common.domain.Estate;
import org.springframework.lang.Nullable;

public interface ScoreCalculator {
    Long getScoreId();
    @Nullable ScoreNormalizer getNormalizer();
    double calculateRawScore(Estate estate);
}