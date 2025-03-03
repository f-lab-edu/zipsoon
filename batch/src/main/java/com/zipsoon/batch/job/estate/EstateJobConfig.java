package com.zipsoon.batch.estate.job.config;

import com.zipsoon.batch.domain.estate.DongCode;
import com.zipsoon.batch.estate.job.processor.EstateItemProcessor;
import com.zipsoon.batch.estate.job.writer.EstateItemWriter;
import com.zipsoon.batch.estate.service.DongCodeService;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.support.ListItemReader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class EstateJobConfig {
    private static final String JOB_NAME = "estateJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;
    private final DongCodeService dongCodeService;
    private final EstateItemProcessor estateItemProcessor;
    private final EstateItemWriter estateItemWriter;

    @Bean(name = JOB_NAME)
    public Job estateScoreJob() {
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(estateStep())
            .build();
    }
    @Bean
    public Step estateStep() {
        return new StepBuilder("estateStep", jobRepository)
            .<String, List<Estate>>chunk(1, transactionManager)
            .reader(dongCodeReader())
            .processor(estateItemProcessor)
            .writer(estateItemWriter)
            .build();
    }

    @Bean
    public ItemReader<String> dongCodeReader() {
        return new ListItemReader<>(dongCodeService.getAllDongCodes().stream()
            .map(DongCode::code)
            .toList());
    }

}
