package com.zipsoon.batch.exception;

import com.zipsoon.common.exception.BaseException;
import com.zipsoon.common.exception.ErrorCode;

import java.util.Map;

public class EstateProcessingException extends BaseException {
    public EstateProcessingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public EstateProcessingException(ErrorCode errorCode, String message) {
        super(errorCode, message, null);
    }

    public EstateProcessingException(ErrorCode errorCode, String message, Map<String, Object> data) {
        super(errorCode, message, data);
    }
}