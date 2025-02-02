package com.zipsoon.batch.job.config;

import com.zipsoon.batch.job.listener.PropertyJobListener;
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
public class PropertyJobConfig {
    private static final String JOB_NAME = "propertyJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final TaskExecutor taskExecutor;

    private final PropertyStepConfig propertyStepConfig;

    @Value("${property.job.partition.size:5}")
    private int partitionSize;

    @Bean
    public Job propertyJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(propertyMasterStep())
            .listener(new PropertyJobListener())
            .build();
    }

    @Bean
    public Step propertyMasterStep() {
        return new StepBuilder("propertyMasterStep", jobRepository)
            .partitioner(propertyStepConfig.propertyWorkerStep().getName(),
                        propertyStepConfig.dongCodePartitioner())
            .step(propertyStepConfig.propertyWorkerStep())
            .gridSize(partitionSize)
            .taskExecutor(taskExecutor)
            .build();
    }
}
