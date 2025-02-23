package com.zipsoon.batch.config;

import com.zipsoon.batch.estate.job.EstateJobRunner;
import com.zipsoon.batch.score.job.EstateScoreJobRunner;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJobRunner implements CommandLineRunner {
    private final EstateJobRunner estateJobRunner;
    private final EstateScoreJobRunner estateScoreJobRunner;

    @Override
    public void run(String... args) throws Exception {
        try {
//            log.info("Starting estate data collection job...");
//            estateJobRunner.run();

            log.info("Starting estate score calculation job...");
            estateScoreJobRunner.run();
        } catch (Exception e) {
            log.error("Failed to run batch jobs", e);
            throw e;
        }
    }
}