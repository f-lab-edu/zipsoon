package com.zipsoon.batch.job.score;

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
public class ScoreJobRunner {
    private final JobLauncher jobLauncher;
    private final Job scoreJob;

    public void run() throws Exception {
        JobParameters params = new JobParametersBuilder()
            .addString("executionTime", LocalDateTime.now().toString())
            .toJobParameters();

        log.info("[BATCH:JOB-START] 매물 점수 계산 작업 시작 - 파라미터: {}", params);
        JobExecution execution = jobLauncher.run(scoreJob, params);
        log.info("[BATCH:JOB-END] 매물 점수 계산 작업 완료 - 상태: {}", execution.getStatus());
    }
}