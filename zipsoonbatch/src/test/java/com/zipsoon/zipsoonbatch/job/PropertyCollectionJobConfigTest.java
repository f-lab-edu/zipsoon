package com.zipsoon.zipsoonbatch.job;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBatchTest
@SpringBootTest
@ActiveProfiles("test")
class PropertyCollectionJobConfigTest {

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;


    @Autowired
    @Qualifier("propertyCollectionJob")
    private Job propertyCollectionJob;

    @BeforeEach
    void setUp() {
        jobLauncherTestUtils.setJob(propertyCollectionJob);
    }

    @Test
    @DisplayName("매물 수집 Job이 정상적으로 실행되어야 한다")
    void jobExecutes() throws Exception {
        // given
        JobParameters jobParameters = new JobParametersBuilder()
                .addString("datetime", "2025-01-22T10:00:00")
                .toJobParameters();

        // when
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(jobParameters);

        // then
        assertThat(jobExecution.getStatus()).isEqualTo(BatchStatus.COMPLETED);
        assertThat(jobExecution.getExitStatus()).isEqualTo(ExitStatus.COMPLETED);

        // Log job execution details
        log.info("Job Execution Details:");
        log.info("Status: {}", jobExecution.getStatus());
        log.info("Exit Description: {}", jobExecution.getExitStatus().getExitDescription());
        log.info("Start Time: {}", jobExecution.getStartTime());
        log.info("End Time: {}", jobExecution.getEndTime());

        jobExecution.getStepExecutions().forEach(stepExecution -> {
            log.info("Step '{}' Details:", stepExecution.getStepName());
            log.info("Read count: {}", stepExecution.getReadCount());
            log.info("Write count: {}", stepExecution.getWriteCount());
            log.info("Commit count: {}", stepExecution.getCommitCount());
        });
    }
}