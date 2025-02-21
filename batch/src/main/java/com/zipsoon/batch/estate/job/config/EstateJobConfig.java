package com.zipsoon.batch.estate.job.config;

import com.zipsoon.batch.estate.job.listener.EstateJobListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EstateJobConfig {
    private static final String JOB_NAME = "estateJob";
    private final JobRepository jobRepository;
    private final EstateStepConfig estateStepConfig;

    @Bean
    public Job estateJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(estateStepConfig.estateWorkerStep())
            .listener(new EstateJobListener())
            .build();
    }

}
