package com.zipsoon.batch.score.job.processor;

import com.zipsoon.batch.infrastructure.whichname.score.ScoreCalculator;
import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.EstateScore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ScoreProcessor implements ItemProcessor<Estate, List<EstateScore>> {
    private final List<ScoreCalculator> calculators;

    @Override
    public List<EstateScore> process(Estate estate) {
        return calculators.stream()
            .map(calculator -> EstateScore.builder()
                .estateId(estate.getId())
                .scoreTypeId(calculator.getScoreId())
                .rawScore(calculator.calculateRawScore(estate))
                .createdAt(LocalDateTime.now())
                .build())
            .toList();
    }
}
