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
public class EstateJobRunner {
    private final JobLauncher jobLauncher;
    private final Job estateJob;

    public void run() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString("executionTime", LocalDateTime.now().toString())
            .toJobParameters();

        log.info("Starting estate job with parameters: {}", params);
        JobExecution execution = jobLauncher.run(estateJob, params);
        log.info("Estate collection job finished with status: {}", execution.getStatus());
    }
}