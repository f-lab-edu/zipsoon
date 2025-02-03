package com.zipsoon.common.exception.domain;

import com.zipsoon.common.exception.BaseException;
import com.zipsoon.common.exception.ErrorCode;

import java.util.Map;

public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message) {
        super(errorCode, message, null);
    }

    public ResourceNotFoundException(ErrorCode errorCode, String message, Map<String, Object> data) {
        super(errorCode, message, data);
    }
}
