package com.zipsoon.batch.job.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.zipsoon.batch.dto.NaverResponseDto;
import com.zipsoon.batch.job.listener.EstateStepListener;
import com.zipsoon.batch.job.partitioner.DongCodePartitioner;
import com.zipsoon.batch.job.processor.EstateItemProcessor;
import com.zipsoon.batch.job.reader.EstateItemReader;
import com.zipsoon.batch.job.reader.JsonEstateItemReader;
import com.zipsoon.batch.job.writer.EstateItemWriter;
import com.zipsoon.batch.service.DongCodeService;
import com.zipsoon.batch.service.NaverClient;
import com.zipsoon.common.domain.EstateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Arrays;
import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EstateStepConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final NaverClient naverClient;
    private final DongCodeService dongCodeService;
    private final EstateItemProcessor estateItemProcessor;
    private final EstateItemWriter estateItemWriter;
    private final ObjectMapper objectMapper;
    private final Environment environment;


    @Bean
    public Step estateWorkerStep() {
        return new StepBuilder("estateWorkerStep", jobRepository)
            .<NaverResponseDto, List<EstateSnapshot>>chunk(1, transactionManager)
            .reader(selectItemReader())
            .processor(estateItemProcessor)
            .writer(estateItemWriter)
            .listener(new EstateStepListener())
            .build();
    }

    @Bean
    public DongCodePartitioner dongCodePartitioner() {
        return new DongCodePartitioner(dongCodeService);
    }

    private ItemReader<NaverResponseDto> selectItemReader() {
        if (isLocalProfile()) {
            log.info("Using JsonEstateItemReader (mock data) for local profile");
            return new JsonEstateItemReader(objectMapper);
        } else {
            log.info("Using EstateItemReader (real API call)");
            return new EstateItemReader(naverClient, dongCodeService);
        }
    }

    private boolean isLocalProfile() {
        return Arrays.asList(environment.getActiveProfiles()).contains("local");
    }

}
