package com.zipsoon.batch.score.job.processor;

import com.zipsoon.batch.score.calculator.ScoreCalculator;
import com.zipsoon.batch.score.model.EstateScore;
import com.zipsoon.common.domain.EstateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreProcessor implements ItemProcessor<EstateSnapshot, List<EstateScore>> {
    private final List<ScoreCalculator> calculators;

    @Override
    public List<EstateScore> process(EstateSnapshot estate) {
        return calculators.stream()
            .map(calculator -> EstateScore.builder()
                .estateSnapshotId(estate.getId())
                .scoreTypeId(calculator.getScoreId())
                .rawScore(calculator.calculateRawScore(estate))
                .createdAt(LocalDateTime.now())
                .build())
            .toList();
    }
}
