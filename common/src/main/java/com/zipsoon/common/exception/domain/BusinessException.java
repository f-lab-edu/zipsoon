package com.zipsoon.common.exception.domain;

import com.zipsoon.common.exception.BaseException;
import com.zipsoon.common.exception.ErrorCode;

import java.util.Map;

public class BusinessException extends BaseException {
    public BusinessException(ErrorCode errorCode) {
        super(errorCode);
    }

    public BusinessException(ErrorCode errorCode, String message) {
        super(errorCode, message, null);
    }

    public BusinessException(ErrorCode errorCode, String message, Map<String, Object> data) {
        super(errorCode, message, data);
    }
}
