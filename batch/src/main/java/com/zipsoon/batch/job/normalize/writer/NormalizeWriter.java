package com.zipsoon.batch.job.normalize.writer;

import com.zipsoon.batch.infrastructure.repository.normalize.NormalizeRepository;
import com.zipsoon.batch.application.service.score.calculator.ScoreCalculator;
import com.zipsoon.batch.domain.score.ScoreType;
import com.zipsoon.common.domain.EstateScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NormalizeWriter implements ItemWriter<ScoreType> {
    private final NormalizeRepository normalizeRepository;
    private final List<ScoreCalculator> calculators;

    @Override
    public void write(Chunk<? extends ScoreType> chunk) {
        for (ScoreType scoreType : chunk) {
            Long scoreTypeId = scoreType.getId();

            ScoreCalculator calculator = calculators.stream()
                .filter(c -> scoreTypeId.equals(c.getScoreId()))
                .findFirst()
                .orElse(null);

            if (calculator == null || calculator.getNormalizer() == null) continue;

            List<EstateScore> scores = normalizeRepository.findByScoreTypeId(scoreTypeId);
            if (scores == null || scores.isEmpty()) continue;

            List<Double> rawScores = scores.stream()
                .map(EstateScore::getRawScore)
                .toList();

            Map<Long, Double> updates = new HashMap<>();
            for (EstateScore score : scores) {
                double rawScore = score.getRawScore();
                double normalizedScore = calculator.getNormalizer().normalize(rawScore, rawScores);
                log.debug("Score ID: {}, Raw: {}, Normalized: {}",
                    score.getId(), String.format("%.10f", rawScore), String.format("%.10f", normalizedScore));
                updates.put(score.getId(), normalizedScore);
            }

            normalizeRepository.updateNormalizedScores(scoreTypeId, updates);
            log.info("Normalized {} scores for type: {}", updates.size(), scoreType.getName());
        }
    }

}