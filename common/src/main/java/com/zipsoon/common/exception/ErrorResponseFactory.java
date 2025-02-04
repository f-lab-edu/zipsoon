package com.zipsoon.common.exception;

import org.springframework.validation.FieldError;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class ErrorResponseFactory {

    private static String generateRequestId() {
        return UUID.randomUUID().toString();
    }

    private static Map<String, Object> wrapData(Object data) {
        if (data instanceof List<?> errors) {
            List<Map<String, String>> formattedErrors = formatErrors(errors);
            return Map.of("errors", formattedErrors);
        }
        if (data instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of("data", data);
    }

    public static ErrorResponse from(ErrorCode errorCode) {
        return new ErrorResponse(
            errorCode.getHttpStatusCode(),
            errorCode.getMessage(),
            generateRequestId(),
            LocalDateTime.now(),
            null
        );
    }

    public static ErrorResponse from(ErrorCode errorCode, Object data) {
        return new ErrorResponse(
            errorCode.getHttpStatusCode(),
            errorCode.getMessage(),
            generateRequestId(),
            LocalDateTime.now(),
            data != null ? wrapData(data) : null
        );
    }

    private static List<Map<String, String>> formatErrors(List<?> errors) {
        return errors.stream()
            .map(error -> {
                if (error instanceof FieldError fieldError) {
                    return Map.of(
                        "field", fieldError.getField(),
                        "message", fieldError.getDefaultMessage(),
                        "rejectedValue", String.valueOf(fieldError.getRejectedValue())
                    );
                }
                return Map.of("error", error.toString());
            })
            .collect(Collectors.toList());
    }

}