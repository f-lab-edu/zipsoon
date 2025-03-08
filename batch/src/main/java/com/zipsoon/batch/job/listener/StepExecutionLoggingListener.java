package com.zipsoon.batch.job.listener;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

/**
 * Spring Batch Step 실행 시 로깅을 위한 리스너
 * Step 시작, 종료 및 통계 정보를 로깅
 */
@Slf4j
public class StepExecutionLoggingListener implements StepExecutionListener {
    
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info("[BATCH:JOB-STEP-START] {} - JobID: {}", 
                stepExecution.getStepName(), 
                stepExecution.getJobExecutionId());
    }

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("[BATCH:JOB-STEP-END] {} - 상태: {} - 읽기: {} - 쓰기: {} - 필터: {} - 커밋: {} - 롤백: {}",
                stepExecution.getStepName(),
                stepExecution.getExitStatus().getExitCode(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount(),
                stepExecution.getFilterCount(),
                stepExecution.getCommitCount(),
                stepExecution.getRollbackCount());
        
        // 타이밍 정보 추가
        long executionTime = 0;
        if (stepExecution.getStartTime() != null && stepExecution.getEndTime() != null) {
            executionTime = stepExecution.getEndTime().getTime() - stepExecution.getStartTime().getTime();
        }
        log.info("[BATCH:JOB-STEP-STATS] {} - 실행 시간: {}ms", 
                stepExecution.getStepName(), executionTime);
                
        // 스킵된 항목이 있는 경우 추가 로깅
        if (stepExecution.getSkipCount() > 0) {
            log.warn("[BATCH:JOB-STEP-WARN] {} - 스킵된 항목: {} (읽기: {}, 처리: {}, 쓰기: {})",
                    stepExecution.getStepName(),
                    stepExecution.getSkipCount(),
                    stepExecution.getReadSkipCount(),
                    stepExecution.getProcessSkipCount(),
                    stepExecution.getWriteSkipCount());
        }
        
        return stepExecution.getExitStatus();
    }
}