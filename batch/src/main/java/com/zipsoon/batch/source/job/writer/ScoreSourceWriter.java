package com.zipsoon.batch.source.job.writer;

import com.zipsoon.batch.source.collector.ScoreSourceCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class ScoreSourceWriter implements ItemWriter<ScoreSourceCollector> {
    @Override
    public void write(Chunk<? extends ScoreSourceCollector> items) {
        for (ScoreSourceCollector collector : items) {
            if (collector.validate()) {
                log.info("Successfully processed source data");
            } else {
                log.error("Failed to validate source data");
            }
        }
    }
}
