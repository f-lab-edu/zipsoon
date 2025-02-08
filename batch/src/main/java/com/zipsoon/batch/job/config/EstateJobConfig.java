package com.zipsoon.batch.job.config;

import com.zipsoon.batch.job.listener.EstateJobListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EstateJobConfig {
    private static final String JOB_NAME = "estateJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor taskExecutor;

    private final EstateStepConfig estateStepConfig;

    @Value("${estate.job.partition.size:5}")
    private int partitionSize;

    @Bean
    public Job estateJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(estateMasterStep())
            .listener(new EstateJobListener())
            .build();
    }

    @Bean
    public Step estateMasterStep() {
        return new StepBuilder("estateMasterStep", jobRepository)
            .partitioner(estateStepConfig.estateWorkerStep().getName(),
                        estateStepConfig.dongCodePartitioner())
            .step(estateStepConfig.estateWorkerStep())
            .gridSize(partitionSize)
            .taskExecutor(taskExecutor)
            .build();
    }
}
