package com.zipsoon.batch.application.service.normalize.normalizer;

import java.util.List;

public interface ScoreNormalizer {
    double normalize(double rawScore, List<Double> scores);
}