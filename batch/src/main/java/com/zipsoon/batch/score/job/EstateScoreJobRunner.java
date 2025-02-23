package com.zipsoon.batch.score.job;

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
public class EstateScoreJobRunner {
    private final JobLauncher jobLauncher;
    private final Job estateScoreJob;

    public void run() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString("executionTime", LocalDateTime.now().toString())
            .toJobParameters();

        log.info("Starting estate score calculation job...");
        JobExecution execution = jobLauncher.run(estateScoreJob, params);
        log.info("Score calculation job finished with status: {}", execution.getStatus());
    }
}