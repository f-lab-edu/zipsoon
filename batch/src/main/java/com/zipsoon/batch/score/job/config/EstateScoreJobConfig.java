package com.zipsoon.batch.score.job.config;

import com.zipsoon.batch.score.calculator.ScoreCalculator;
import com.zipsoon.batch.score.job.processor.EstateScoreProcessor;
import com.zipsoon.batch.score.job.reader.EstateScoreReader;
import com.zipsoon.batch.score.job.writer.EstateScoreWriter;
import com.zipsoon.batch.score.model.EstateScore;
import com.zipsoon.batch.score.model.ScoreType;
import com.zipsoon.batch.score.repository.ScoreTypeRepository;
import com.zipsoon.common.domain.EstateSnapshot;
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
import java.util.Map;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class EstateScoreJobConfig {
    private static final String JOB_NAME = "estateScoreJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final ScoreTypeRepository scoreTypeRepository;
    private final List<ScoreCalculator> calculators;
    private final EstateScoreReader estateScoreReader;
    private final EstateScoreWriter estateScoreWriter;

    @Bean(name = JOB_NAME)
    public Job estateScoreJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(estateScoreStep())
            .build();
    }

    @Bean
    public Step estateScoreStep() {
        return new StepBuilder("estateScoreStep", jobRepository)
            .<EstateSnapshot, List<EstateScore>>chunk(100, transactionManager)
            .reader(estateScoreReader)
            .processor(estateScoreProcessor())
            .writer(estateScoreWriter)
            .build();
    }

    @Bean
    public EstateScoreProcessor estateScoreProcessor() {
        Map<String, Long> scoreTypeIds = scoreTypeRepository.findAllActive()
            .stream()
            .collect(Collectors.toMap(
                ScoreType::getName,
                ScoreType::getId
            ));

        return new EstateScoreProcessor(calculators, scoreTypeIds);
    }
}