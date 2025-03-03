package com.zipsoon.batch.source.job.processor;

import com.zipsoon.batch.infrastructure.whichname.source.ScoreSourceCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScoreSourceProcessor implements ItemProcessor<ScoreSourceCollector, ScoreSourceCollector> {
    @Override
    public ScoreSourceCollector process(ScoreSourceCollector collector) {
        log.info("Processing source data collection");
        collector.create();
        if (!collector.wasUpdated()) {
            return collector;
        }
        
        collector.collect();
        collector.preprocess();
        return collector;
    }
}
