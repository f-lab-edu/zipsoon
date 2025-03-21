package com.zipsoon.batch.job.migration;

import com.zipsoon.batch.infrastructure.processor.source.collector.DongCodeSourceCollector;
import com.zipsoon.batch.job.listener.StepExecutionLoggingListener;
import com.zipsoon.batch.job.migration.tasklet.DatabaseMigrationTasklet;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 데이터베이스 초기화 작업 설정
 * 이 작업은 다른 모든 작업 전에 실행되어야 함
 * 1. 법정동코드 테이블 초기화 (dongcodesInitStep) - 기존 DongCodeSourceCollector 활용
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
    private final DongCodeSourceCollector dongCodeSourceCollector;

    @Bean(name = JOB_NAME)
    public Job databaseInitJob() {
        log.info("[BATCH:JOB-CONFIG] 데이터베이스 초기화 작업(databaseInitJob) 구성");
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(dongcodesInitStep())
            .next(databaseMigrationStep())
            .build();
    }
    
    @Bean
    public Step dongcodesInitStep() {
        log.info("[BATCH:STEP-CONFIG] 법정동코드 초기화 단계(dongcodesInitStep) 구성");
        return new StepBuilder("dongcodesInitStep", jobRepository)
            .tasklet(dongcodesInitTasklet(), transactionManager)
            .listener(new StepExecutionLoggingListener())
            .build();
    }
    
    @Bean
    public Tasklet dongcodesInitTasklet() {
        return (contribution, chunkContext) -> {
            try {
                log.info("[BATCH:TASKLET-START] 법정동코드 테이블 초기화 시작");
                
                dongCodeSourceCollector.create();
                dongCodeSourceCollector.collect();
                
                log.info("[BATCH:TASKLET-END] 법정동코드 테이블 초기화 완료");
                return RepeatStatus.FINISHED;
            } catch (Exception e) {
                log.error("[BATCH:TASKLET-ERR] 법정동코드 테이블 초기화 실패: {}", e.getMessage(), e);
                throw e;
            }
        };
    }
    
    @Bean
    public Step databaseMigrationStep() {
        log.info("[BATCH:STEP-CONFIG] 데이터베이스 마이그레이션 단계(databaseMigrationStep) 구성");
        return new StepBuilder("databaseMigrationStep", jobRepository)
            .tasklet(databaseMigrationTasklet, transactionManager)
            .listener(new StepExecutionLoggingListener())
            .build();
    }
}