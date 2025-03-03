package com.zipsoon.batch;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class NormalizeJobRunner {
    private final JobLauncher jobLauncher;
    private final Job normalizeJob;

    public void run() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString("executionTime", LocalDateTime.now().toString())
            .toJobParameters();

        log.info("Starting score normalization job...");
        JobExecution execution = jobLauncher.run(normalizeJob, params);
        log.info("Score normalization job finished with status: {}", execution.getStatus());
    }
}