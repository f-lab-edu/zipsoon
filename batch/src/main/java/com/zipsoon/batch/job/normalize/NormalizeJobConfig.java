package com.zipsoon.batch.normalize.job.config;

import com.zipsoon.batch.normalize.job.processor.NormalizeProcessor;
import com.zipsoon.batch.normalize.job.reader.NormalizeReader;
import com.zipsoon.batch.normalize.job.writer.NormalizeWriter;
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
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(normalizeScoresStep())
            .build();
    }

    @Bean
    public Step normalizeScoresStep() {
        return new StepBuilder("normalizeStep", jobRepository)
            .<ScoreType, ScoreType>chunk(1, transactionManager)
            .reader(normalizeReader)
            .processor(normalizeProcessor)
            .writer(normalizeWriter)
            .build();
    }
}