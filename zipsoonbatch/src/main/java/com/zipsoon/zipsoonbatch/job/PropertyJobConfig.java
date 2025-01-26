package com.zipsoon.zipsoonbatch.job;

import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.job.processor.PropertyProcessor;
import com.zipsoon.zipsoonbatch.job.reader.NaverResponseDto;
import com.zipsoon.zipsoonbatch.job.reader.NaverClient;
import com.zipsoon.zipsoonbatch.job.reader.PropertyReader;
import com.zipsoon.zipsoonbatch.job.writer.PropertyWriter;
import com.zipsoon.zipsoonbatch.repository.PropertyHistoryRepository;
import com.zipsoon.zipsoonbatch.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.javers.core.Javers;
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
public class PropertyJobConfig {
    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NaverClient naverClient;
    private final PropertyRepository propertyRepository;
    private final PropertyHistoryRepository propertyHistoryRepository;

    @Bean
    public Job propertyCollectionJob() {
        return new JobBuilder("propertyJob", jobRepository)
            .start(propertyCollectionStep())
            .build();
    }

    @Bean
    public Step propertyCollectionStep() {
        return new StepBuilder("propertyCollectionStep", jobRepository)
            .<NaverResponseDto.ArticleDto, Property>chunk(CHUNK_SIZE, transactionManager)
            .reader(propertyReader())
            .processor(propertyProcessor())
            .writer(propertyWriter())
            .build();
    }

    @Bean
    public PropertyReader propertyReader() {
        return new PropertyReader(naverClient);
    }

    @Bean
    public PropertyProcessor propertyProcessor() {
        return new PropertyProcessor();
    }

    @Bean
    public PropertyWriter propertyWriter() {
        return new PropertyWriter(propertyRepository, propertyHistoryRepository);
    }
}