package com.zipsoon.batch.job.normalize.processor;

import com.zipsoon.common.domain.score.ScoreType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NormalizeProcessor implements ItemProcessor<ScoreType, ScoreType> {
    @Override
    public ScoreType process(ScoreType scoreType) {
        log.info("[BATCH:STEP-PROCESSOR] 점수 유형 정규화 처리 중: {}", scoreType.getName());
        return scoreType;
    }
}