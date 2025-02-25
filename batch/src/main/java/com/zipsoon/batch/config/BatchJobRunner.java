package com.zipsoon.batch.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class BatchJobRunner implements CommandLineRunner {
    private final ScheduleConfig scheduleConfig;

    @Override
    public void run(String... args) throws Exception {
        try {
            log.info("Starting initial batch job sequence on application startup");
            scheduleConfig.runEstateJobScheduled();
        } catch (Exception e) {
            log.error("Failed to run initial batch jobs", e);
        }
    }
}