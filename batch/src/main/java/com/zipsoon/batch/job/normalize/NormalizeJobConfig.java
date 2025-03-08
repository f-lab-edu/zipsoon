package com.zipsoon.batch.job.normalize;

import com.zipsoon.batch.job.listener.StepExecutionLoggingListener;
import com.zipsoon.batch.job.normalize.processor.NormalizeProcessor;
import com.zipsoon.batch.job.normalize.reader.NormalizeReader;
import com.zipsoon.batch.job.normalize.writer.NormalizeWriter;
import com.zipsoon.batch.domain.score.ScoreType;
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

@Configuration
@RequiredArgsConstructor
@Slf4j
public class NormalizeJobConfig {
    private static final String JOB_NAME = "normalizeJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final NormalizeReader normalizeReader;
    private final NormalizeProcessor normalizeProcessor;
    private final NormalizeWriter normalizeWriter;

    @Bean(name = JOB_NAME)
    public Job normalizeJob() {
        log.info("[BATCH:JOB-CONFIG] 정규화 작업(normalizeJob) 구성");
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(normalizeProcessingStep())
            .build();
    }

    @Bean
    public Step normalizeProcessingStep() {
        log.info("[BATCH:STEP-CONFIG] 정규화 단계(normalizeProcessingStep) 구성");
        return new StepBuilder("normalizeProcessingStep", jobRepository)
            .<ScoreType, ScoreType>chunk(1, transactionManager)
            .reader(normalizeReader)
            .processor(normalizeProcessor)
            .writer(normalizeWriter)
            .listener(new StepExecutionLoggingListener())
            .build();
    }
}