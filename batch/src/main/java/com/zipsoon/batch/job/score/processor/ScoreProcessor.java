package com.zipsoon.batch.job.score.processor;

import com.zipsoon.batch.application.service.score.calculator.ScoreCalculator;
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
        log.debug("[BATCH:STEP-PROCESSOR] 매물 ID {} 점수 계산 시작", estate.getId());
        
        List<EstateScore> scores = calculators.stream()
            .map(calculator -> {
                double rawScore = calculator.calculateRawScore(estate);
                log.debug("[BATCH:STEP-PROCESSOR] 매물 ID {} - {} 점수: {}", 
                        estate.getId(), calculator.getScoreId(), rawScore);
                return EstateScore.of(
                    estate.getId(),
                    calculator.getScoreId(),
                    rawScore
                );
            })
            .toList();
            
        log.debug("[BATCH:STEP-PROCESSOR] 매물 ID {} 점수 계산 완료 - {}개 유형 처리됨", 
                estate.getId(), scores.size());
        return scores;
    }
}
