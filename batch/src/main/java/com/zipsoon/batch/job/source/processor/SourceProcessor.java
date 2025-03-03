package com.zipsoon.batch.job.source.processor;

import com.zipsoon.batch.application.service.source.collector.SourceCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SourceProcessor implements ItemProcessor<SourceCollector, SourceCollector> {
    @Override
    public SourceCollector process(SourceCollector collector) {
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
