package com.zipsoon.batch.job.estate;

import com.zipsoon.batch.job.estate.processor.EstateItemProcessor;
import com.zipsoon.batch.job.estate.reader.EstateItemReader;
import com.zipsoon.batch.job.estate.writer.EstateItemWriter;
import com.zipsoon.batch.job.listener.StepExecutionLoggingListener;
import com.zipsoon.common.domain.Estate;
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

import java.util.List;

/**
 * Estate 관련 배치 작업 설정
 * 새 데이터 수집 및 저장 처리 (스냅샷 작업은 별도 DatabaseInitJob에서 처리)
 */
@Configuration
@RequiredArgsConstructor
@Slf4j
public class EstateJobConfig {
    private static final String JOB_NAME = "estateJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final EstateItemReader estateItemReader;
    private final EstateItemProcessor estateItemProcessor;
    private final EstateItemWriter estateItemWriter;

    @Bean(name = JOB_NAME)
    public Job estateScoreJob() {
        log.info("[BATCH:JOB-CONFIG] 매물 수집 작업(estateJob) 구성");
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(estateBatchStep())
            .build();
    }
    
    @Bean
    public Step estateBatchStep() {
        log.info("[BATCH:STEP-CONFIG] 매물 수집 단계(estateBatchStep) 구성");
        return new StepBuilder("estateBatchStep", jobRepository)
            .<String, List<Estate>>chunk(1, transactionManager)
            .reader(estateItemReader)
            .processor(estateItemProcessor)
            .writer(estateItemWriter)
            .listener(new StepExecutionLoggingListener())
            .build();
    }
}
