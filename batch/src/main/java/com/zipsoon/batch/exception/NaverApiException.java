package com.zipsoon.batch.exception;

import com.zipsoon.common.exception.BaseException;
import com.zipsoon.common.exception.ErrorCode;

import java.util.Map;

public class NaverApiException extends BaseException {
    public NaverApiException(ErrorCode errorCode) {
        super(errorCode);
    }

    public NaverApiException(ErrorCode errorCode, String message) {
        super(errorCode, message, null);
    }

    public NaverApiException(ErrorCode errorCode, String message, Map<String, Object> data) {
        super(errorCode, message, data);
    }
}