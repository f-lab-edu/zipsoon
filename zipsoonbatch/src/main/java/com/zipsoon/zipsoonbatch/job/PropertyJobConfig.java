package com.zipsoon.zipsoonbatch.job;

import com.zipsoon.zipsoonbatch.domain.Property;
import com.zipsoon.zipsoonbatch.job.processor.PropertyProcessor;
import com.zipsoon.zipsoonbatch.job.reader.NaverArticleResponseDto;
import com.zipsoon.zipsoonbatch.job.reader.NaverLandClient;
import com.zipsoon.zipsoonbatch.job.reader.PropertyReader;
import com.zipsoon.zipsoonbatch.job.writer.PropertyWriter;
import com.zipsoon.zipsoonbatch.repository.PropertyHistoryRepository;
import com.zipsoon.zipsoonbatch.repository.PropertyRepository;
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
public class PropertyCollectionJobConfig {
    private static final int CHUNK_SIZE = 10;

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NaverLandClient naverLandClient;
    private final PropertyRepository propertyRepository;
    private final PropertyHistoryRepository propertyHistoryRepository;

    @Bean
    public Job propertyCollectionJob() {
        return new JobBuilder("propertyCollectionJob", jobRepository)
            .start(propertyCollectionStep())
            .build();
    }

    @Bean
    public Step propertyCollectionStep() {
        return new StepBuilder("propertyCollectionStep", jobRepository)
            .<NaverArticleResponseDto.ArticleDto, Property>chunk(CHUNK_SIZE, transactionManager)
            .reader(propertyReader())
            .processor(propertyProcessor())
            .writer(propertyWriter())
            .build();
    }

    @Bean
    public PropertyReader propertyReader() {
        return new PropertyReader(naverLandClient);
    }

    @Bean
    public PropertyProcessor propertyProcessor() {
        return new PropertyProcessor(propertyRepository);
    }

    @Bean
    public PropertyWriter propertyWriter() {
        return new PropertyWriter(propertyRepository, propertyHistoryRepository);
    }
}