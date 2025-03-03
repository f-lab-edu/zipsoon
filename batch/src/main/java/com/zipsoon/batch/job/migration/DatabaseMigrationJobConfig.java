package com.zipsoon.batch.job.migration;

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
 * 데이터베이스 스냅샷 이동 및 비우기 작업 설정
 * 이 작업은 다른 모든 작업 전에 실행되어야 함
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class DatabaseMigrationJobConfig {
    private static final String JOB_NAME = "databaseMigrationJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DatabaseMigrationTasklet databaseMigrationTasklet;

    @Bean(name = JOB_NAME)
    public Job databaseMigrationJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(databaseMigrationStep())
            .build();
    }
    
    @Bean
    public Step databaseMigrationStep() {
        return new StepBuilder("databaseMigrationStep", jobRepository)
            .tasklet(databaseMigrationTasklet, transactionManager)
            .build();
    }
}