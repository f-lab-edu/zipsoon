package com.zipsoon.batch.job;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EstateJobRunner implements CommandLineRunner {
    private final JobLauncher jobLauncher;
    private final Job estateJob;

    @Override
    public void run(String... args) throws Exception {
        JobParameters jobParameters = new JobParametersBuilder()
            .addString("executionTime", LocalDateTime.now().toString())
            .toJobParameters();

        log.info("Starting estate job with parameters: {}", jobParameters);
        JobExecution execution = jobLauncher.run(estateJob, jobParameters);
        log.info("Job finished with status: {}", execution.getStatus());
    }
}