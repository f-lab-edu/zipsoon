package com.zipsoon.batch.score.job.config;

import com.zipsoon.batch.score.job.processor.ScoreProcessor;
import com.zipsoon.batch.score.job.reader.ScoreReader;
import com.zipsoon.batch.score.job.writer.ScoreWriter;
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
    public Job estateScoreJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(estateScoreStep())
            .build();
    }

    @Bean
    public Step estateScoreStep() {
        return new StepBuilder("estateScoreStep", jobRepository)
            .<Estate, List<EstateScore>>chunk(100, transactionManager)
            .reader(scoreReader)
            .processor(scoreProcessor)
            .writer(scoreWriter)
            .build();
    }
}