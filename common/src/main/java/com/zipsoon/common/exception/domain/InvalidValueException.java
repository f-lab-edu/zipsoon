package com.zipsoon.common.exception.domain;

import com.zipsoon.common.exception.BaseException;
import com.zipsoon.common.exception.ErrorCode;

import java.util.Map;

public class InvalidValueException extends BaseException {
    public InvalidValueException(ErrorCode errorCode) {
        super(errorCode);
    }

    public InvalidValueException(ErrorCode errorCode, String message) {
        super(errorCode, message, null);
    }

    public InvalidValueException(ErrorCode errorCode, String message, Map<String, Object> data) {
        super(errorCode, message, data);
    }
}
