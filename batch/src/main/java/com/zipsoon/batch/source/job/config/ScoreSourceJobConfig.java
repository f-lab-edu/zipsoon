package com.zipsoon.batch.source.job.config;

import com.zipsoon.batch.source.collector.ScoreSourceCollector;
import com.zipsoon.batch.source.job.processor.ScoreSourceProcessor;
import com.zipsoon.batch.source.job.reader.ScoreSourceReader;
import lombok.RequiredArgsConstructor;
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
public class ScoreSourceJobConfig {
    private static final String JOB_NAME = "sourceJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ScoreSourceReader scoreSourceReader;
    private final ScoreSourceProcessor scoreSourceProcessor;

    @Bean(name = JOB_NAME)
    public Job sourceJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(sourceStep())
            .build();
    }

    @Bean
    public Step sourceStep() {
        return new StepBuilder("sourceStep", jobRepository)
            .<ScoreSourceCollector, ScoreSourceCollector>chunk(1, transactionManager)
            .reader(scoreSourceReader)
            .processor(scoreSourceProcessor)
            .writer(chunk -> {})    // writer 없음
            .build();
    }
}
