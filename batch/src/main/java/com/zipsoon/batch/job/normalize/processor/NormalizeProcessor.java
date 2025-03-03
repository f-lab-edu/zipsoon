package com.zipsoon.batch.job.normalize.processor;

import com.zipsoon.batch.domain.score.ScoreType;
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
        log.info("Processing normalization for score type: {}", scoreType.getName());
        return scoreType;
    }
}