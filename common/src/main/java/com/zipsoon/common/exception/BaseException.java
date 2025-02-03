package com.zipsoon.common.exception;

import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
public abstract class BaseException extends RuntimeException {
    private final ErrorCode errorCode;
    private final LocalDateTime timestamp;
    private final Map<String, Object> data;

    protected BaseException(ErrorCode errorCode) {
        this(errorCode, new HashMap<>());
    }

    protected BaseException(ErrorCode errorCode, Map<String, Object> data) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.data = data != null ? data : new HashMap<>();
    }

    protected BaseException(ErrorCode errorCode, String message, Map<String, Object> data) {
        super(message);
        this.errorCode = errorCode;
        this.timestamp = LocalDateTime.now();
        this.data = data != null ? data : new HashMap<>();
    }
}
