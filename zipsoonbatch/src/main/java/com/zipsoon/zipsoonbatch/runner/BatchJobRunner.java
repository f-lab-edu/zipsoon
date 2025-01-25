package com.zipsoon.zipsoonbatch.runner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
@RequiredArgsConstructor
public class BatchJobRunner implements CommandLineRunner {
    private final JobLauncher jobLauncher;
    private final Job propertyCollectionJob;  // Job 이름과 동일한 빈을 주입받음

    @Override
    public void run(String... args) throws Exception {
        log.info("Starting batch job at {}", LocalDateTime.now());

        JobParameters jobParameters = new JobParametersBuilder()
            .addString("datetime", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME))
            .toJobParameters();

        jobLauncher.run(propertyCollectionJob, jobParameters);
    }
}