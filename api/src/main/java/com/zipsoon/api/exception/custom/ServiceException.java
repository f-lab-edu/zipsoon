package com.zipsoon.api.exception.custom;

import com.zipsoon.api.exception.model.ErrorCode;
import com.zipsoon.api.exception.model.ErrorDetail;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class ServiceException extends RuntimeException {
    private final ErrorCode errorCode;
    private final List<ErrorDetail> details;

    public ServiceException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = new ArrayList<>();
    }

    public ServiceException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
        this.details = new ArrayList<>();
    }

    public ServiceException(ErrorCode errorCode, List<ErrorDetail> details) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
        this.details = details;
    }

    public ServiceException(ErrorCode errorCode, String message, List<ErrorDetail> details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }

}
