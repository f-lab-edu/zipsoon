package com.zipsoon.batch.score.calculator;

import com.zipsoon.common.domain.EstateSnapshot;

public interface ScoreCalculator {
    String getScoreTypeName();
    double calculate(EstateSnapshot estate);
}
