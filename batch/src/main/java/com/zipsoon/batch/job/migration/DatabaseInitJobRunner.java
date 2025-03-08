package com.zipsoon.batch.job.migration;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * 데이터베이스 초기화 작업 실행기
 * DatabaseInitTasklet과 DatabaseMigrationTasklet 실행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseInitJobRunner {
    private final JobLauncher jobLauncher;
    private final Job databaseInitJob;

    public void run() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString("executionTime", LocalDateTime.now().toString())
            .toJobParameters();

        log.info("[BATCH:JOB-START] 데이터베이스 초기화 작업 시작 - 파라미터: {}", params);
        JobExecution execution = jobLauncher.run(databaseInitJob, params);
        log.info("[BATCH:JOB-END] 데이터베이스 초기화 작업 완료 - 상태: {}", execution.getStatus());
    }
}