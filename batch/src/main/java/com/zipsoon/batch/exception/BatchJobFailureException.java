package com.zipsoon.batch.exception;

import com.zipsoon.common.exception.BaseException;
import com.zipsoon.common.exception.ErrorCode;

import java.util.Map;

public class BatchJobFailureException extends BaseException {
    public BatchJobFailureException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BatchJobFailureException(ErrorCode errorCode, String message) {
        super(errorCode, message, null);
    }

    public BatchJobFailureException(ErrorCode errorCode, String message, Map<String, Object> data) {
        super(errorCode, message, data);
    }
}