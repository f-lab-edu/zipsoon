package com.zipsoon.batch.job.source;

import com.zipsoon.batch.application.service.source.collector.SourceCollector;
import com.zipsoon.batch.job.listener.StepExecutionLoggingListener;
import com.zipsoon.batch.job.source.processor.SourceProcessor;
import com.zipsoon.batch.job.source.reader.SourceReader;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class SourceJobConfig {
    private static final String JOB_NAME = "sourceJob";

    private final JobRepository jobRepository;
    private final PlatformTransactionManager transactionManager;

    private final SourceReader sourceReader;
    private final SourceProcessor sourceProcessor;

    @Bean(name = JOB_NAME)
    public Job sourceJob() {
        log.info("[BATCH:JOB-CONFIG] 소스 데이터 수집 작업(sourceJob) 구성");
        return new JobBuilder(JOB_NAME, jobRepository)
            .start(sourceProcessingStep())
            .build();
    }

    @Bean
    public Step sourceProcessingStep() {
        log.info("[BATCH:STEP-CONFIG] 소스 데이터 수집 단계(sourceProcessingStep) 구성");
        return new StepBuilder("sourceProcessingStep", jobRepository)
            .<SourceCollector, SourceCollector>chunk(1, transactionManager)
            .reader(sourceReader)
            .processor(sourceProcessor)
            .writer(chunk -> {
                // writer 없음 - 각 collector가 자체적으로 저장 처리
                log.debug("[BATCH:STEP-WRITER] 소스 데이터 수집 완료 (writer 없음)");
            })
            .listener(new StepExecutionLoggingListener())
            .build();
    }
}
