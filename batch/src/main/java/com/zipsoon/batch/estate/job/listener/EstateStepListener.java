package com.zipsoon.batch.estate.job.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class EstateStepListener implements StepExecutionListener {
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("Step starting: {}", stepExecution.getStepName());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("Step finished. Read count: {}, Write count: {}",
                stepExecution.getReadCount(),
                stepExecution.getWriteCount());
        return stepExecution.getExitStatus();
    }
}