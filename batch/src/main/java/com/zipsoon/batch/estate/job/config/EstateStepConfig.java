package com.zipsoon.batch.estate.job.config;

import com.zipsoon.batch.estate.domain.DongCode;
import com.zipsoon.batch.estate.job.listener.EstateStepListener;
import com.zipsoon.batch.estate.job.processor.EstateItemProcessor;
import com.zipsoon.batch.estate.job.writer.EstateItemWriter;
import com.zipsoon.batch.estate.service.DongCodeService;
import com.zipsoon.common.domain.EstateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EstateStepConfig {
    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DongCodeService dongCodeService;
    private final EstateItemProcessor estateItemProcessor;
    private final EstateItemWriter estateItemWriter;


    @Bean
    public Step estateWorkerStep() {
        return new StepBuilder("estateWorkerStep", jobRepository)
            .<String, List<EstateSnapshot>>chunk(1, transactionManager)
            .reader(dongCodeReader())
            .processor(estateItemProcessor)
            .writer(estateItemWriter)
            .listener(new EstateStepListener())
            .build();
    }

    @Bean
    public ItemReader<String> dongCodeReader() {
        return new ListItemReader<>(dongCodeService.getAllDongCodes().stream()
            .map(DongCode::code)
            .collect(Collectors.toList()));
    }

}
