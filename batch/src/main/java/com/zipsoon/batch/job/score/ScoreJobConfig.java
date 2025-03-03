package com.zipsoon.batch.job.score;

import com.zipsoon.batch.job.score.processor.ScoreProcessor;
import com.zipsoon.batch.job.score.reader.ScoreReader;
import com.zipsoon.batch.job.score.writer.ScoreWriter;
import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.EstateScore;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class ScoreJobConfig {
    private static final String JOB_NAME = "scoreJob";
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final ScoreReader scoreReader;
    private final ScoreProcessor scoreProcessor;
    private final ScoreWriter scoreWriter;

    @Bean(name = JOB_NAME)
    public Job scoreJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(scoreProcessingStep())
            .build();
    }

    @Bean
    public Step scoreProcessingStep() {
        return new StepBuilder("scoreProcessingStep", jobRepository)
            .<Estate, List<EstateScore>>chunk(100, transactionManager)
            .reader(scoreReader)
            .processor(scoreProcessor)
            .writer(scoreWriter)
            .build();
    }
}