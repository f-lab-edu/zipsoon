package com.zipsoon.batch.job.config;

import com.zipsoon.batch.dto.NaverResponseDto;
import com.zipsoon.batch.job.partitioner.DongCodePartitioner;
import com.zipsoon.batch.job.listener.PropertyStepListener;
import com.zipsoon.batch.job.processor.PropertyItemProcessor;
import com.zipsoon.batch.job.reader.PropertyItemReader;
import com.zipsoon.batch.service.DongCodeService;
import com.zipsoon.batch.job.writer.PropertyItemWriter;
import com.zipsoon.common.domain.EstateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class PropertyStepConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DongCodeService dongCodeService;

    private final PropertyItemReader propertyItemReader;
    private final PropertyItemProcessor propertyItemProcessor;
    private final PropertyItemWriter propertyItemWriter;

    @Bean
    public Step propertyWorkerStep() {
        return new StepBuilder("propertyWorkerStep", jobRepository)
            .<NaverResponseDto, List<EstateSnapshot>>chunk(1, transactionManager)
            .reader(propertyItemReader)
            .processor(propertyItemProcessor)
            .writer(propertyItemWriter)
            .listener(new PropertyStepListener())
            .build();
    }

    @Bean
    public DongCodePartitioner dongCodePartitioner() {
        return new DongCodePartitioner(dongCodeService);
    }
}
