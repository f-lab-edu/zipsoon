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
 * 데이터베이스 마이그레이션 작업 실행기
 * 배치 작업 시작 전에 호출되어 estate와 estate_score 테이블의 스냅샷 작업을 수행
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DatabaseMigrationJobRunner {
    private final JobLauncher jobLauncher;
    private final Job databaseMigrationJob;

    public void run() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString("executionTime", LocalDateTime.now().toString())
            .toJobParameters();

        log.info("Starting database migration job with parameters: {}", params);
        JobExecution execution = jobLauncher.run(databaseMigrationJob, params);
        log.info("Database migration job finished with status: {}", execution.getStatus());
    }
}