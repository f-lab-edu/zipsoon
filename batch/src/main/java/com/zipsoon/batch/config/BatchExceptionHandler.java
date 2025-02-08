package com.zipsoon.batch.config;

import com.zipsoon.batch.exception.BatchJobFailureException;
import com.zipsoon.batch.exception.NaverApiException;
import com.zipsoon.batch.exception.EstateProcessingException;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.stereotype.Component;

@Component
public class BatchExceptionHandler implements StepExecutionListener {

    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        if (stepExecution.getFailureExceptions().isEmpty()) {
            return ExitStatus.COMPLETED;
        }

        Throwable cause = stepExecution.getFailureExceptions().get(0);
        if (cause instanceof JobInstanceAlreadyCompleteException) {
            return ExitStatus.NOOP;
        } else if (cause instanceof NaverApiException) {
            return new ExitStatus("API_FAILURE");
        } else if (cause instanceof EstateProcessingException) {
            return new ExitStatus("PROCESSING_FAILURE");
        } else if (cause instanceof BatchJobFailureException) {
            return new ExitStatus("JOB_FAILURE");
        }

        return ExitStatus.FAILED;
    }
}
