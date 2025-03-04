package com.zipsoon.batch.job.migration;

import com.zipsoon.batch.job.migration.tasklet.DatabaseDongcodesInitTasklet;
import com.zipsoon.batch.job.migration.tasklet.DatabaseMigrationTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 데이터베이스 초기화 작업 설정
 * 이 작업은 다른 모든 작업 전에 실행되어야 함
 * 1. 법정동코드 테이블 초기화 (dongcodesInitStep)
 * 2. 매물 데이터 스냅샷 이동 및 비우기 (migrationStep)
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseInitJobConfig {
    private static final String JOB_NAME = "databaseInitJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DatabaseMigrationTasklet databaseMigrationTasklet;
    private final DatabaseDongcodesInitTasklet databaseDongcodesInitTasklet;

    @Bean(name = JOB_NAME)
    public Job databaseInitJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(dongcodesInitStep())
            .next(databaseMigrationStep())
            .build();
    }
    
    @Bean
    public Step dongcodesInitStep() {
        return new StepBuilder("dongcodesInitStep", jobRepository)
            .tasklet(databaseDongcodesInitTasklet, transactionManager)
            .build();
    }
    
    @Bean
    public Step databaseMigrationStep() {
        return new StepBuilder("databaseMigrationStep", jobRepository)
            .tasklet(databaseMigrationTasklet, transactionManager)
            .build();
    }
}