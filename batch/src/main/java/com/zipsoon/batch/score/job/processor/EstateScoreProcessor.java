package com.zipsoon.batch.score.job.processor;

import com.zipsoon.batch.score.calculator.ScoreCalculator;
import com.zipsoon.batch.score.model.EstateScore;
import com.zipsoon.common.domain.EstateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@RequiredArgsConstructor
public class EstateScoreProcessor implements ItemProcessor<EstateSnapshot, List<EstateScore>> {
    private final List<ScoreCalculator> calculators;
    private final Map<String, Long> scoreTypeIds;

    @Override
    public List<EstateScore> process(EstateSnapshot estate) {
        return calculators.stream()
            .map(calculator -> {
                String typeName = calculator.getScoreTypeName();
                Long typeId = scoreTypeIds.get(typeName);

                if (typeId == null) {
                    log.warn("Score type not found: {}", typeName);
                    return null;
                }

                return EstateScore.builder()
                    .estateSnapshotId(estate.getId())
                    .scoreTypeId(typeId)
                    .score(BigDecimal.valueOf(
                        calculator.calculate(estate)
                    ))
                    .createdAt(LocalDateTime.now())
                    .build();
            })
            .filter(Objects::nonNull)
            .toList();
    }
}
