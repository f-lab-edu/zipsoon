package com.zipsoon.batch.job.source;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class SourceJobRunner {
    private final JobLauncher jobLauncher;
    private final Job sourceJob;

    public void run() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString("executionTime", LocalDateTime.now().toString())
            .toJobParameters();

        log.info("[BATCH:JOB-START] 소스 데이터 수집 작업 시작 - 파라미터: {}", params);
        JobExecution execution = jobLauncher.run(sourceJob, params);
        log.info("[BATCH:JOB-END] 소스 데이터 수집 작업 완료 - 상태: {}", execution.getStatus());
    }
}
