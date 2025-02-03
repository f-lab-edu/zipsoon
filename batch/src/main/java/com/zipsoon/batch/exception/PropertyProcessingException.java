package com.zipsoon.batch.exception;

import com.zipsoon.common.exception.BaseException;
import com.zipsoon.common.exception.ErrorCode;

import java.util.Map;

public class PropertyProcessingException extends BaseException {
    public PropertyProcessingException(ErrorCode errorCode) {
        super(errorCode);
    }

    public PropertyProcessingException(ErrorCode errorCode, String message) {
        super(errorCode, message, null);
    }

    public PropertyProcessingException(ErrorCode errorCode, String message, Map<String, Object> data) {
        super(errorCode, message, data);
    }
}