package com.zipsoon.api.exception.model;

import com.zipsoon.api.logger.filter.RequestIdUtil;
import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;

@SuppressWarnings("ALL")
public class ErrorResponseFactory {

    private static List<ErrorDetail> convertFieldErrors(List<FieldError> fieldErrors) {
        return fieldErrors.stream()
            .map(error -> new ErrorDetail(
                error.getField(),
                error.getDefaultMessage(),
                "FIELD_VALIDATION_ERROR"
            ))
            .toList();
    }

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
            errorCode.getHttpStatusCode(),
            errorCode.getMessage(),
            RequestIdUtil.getRequestId(),
            LocalDateTime.now(),
            null
        );
    }

    public static ErrorResponse from(ErrorCode errorCode, List<FieldError> fieldErrors) {
        return new ErrorResponse(
            errorCode.getHttpStatusCode(),
            errorCode.getMessage(),
            RequestIdUtil.getRequestId(),
            LocalDateTime.now(),
            convertFieldErrors(fieldErrors)
        );
    }

    public static ErrorResponse from(ErrorCode errorCode, List<ErrorDetail> details, String message) {
        return new ErrorResponse(
            errorCode.getHttpStatusCode(),
            message,
            RequestIdUtil.getRequestId(),
            LocalDateTime.now(),
            details
        );
    }

    public static ErrorResponse from(ErrorCode errorCode, String customMessage) {
        return new ErrorResponse(
            errorCode.getHttpStatusCode(),
            customMessage != null ? customMessage : errorCode.getMessage(),
            RequestIdUtil.getRequestId(),
            LocalDateTime.now(),
            null
        );
    }
}