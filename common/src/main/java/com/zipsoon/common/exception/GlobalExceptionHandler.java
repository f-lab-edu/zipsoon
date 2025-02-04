package com.zipsoon.common.exception;

import com.zipsoon.common.exception.domain.InvalidValueException;
import com.zipsoon.common.exception.domain.ResourceNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();
        return ResponseEntity
            .status(ErrorCode.BAD_REQUEST.getHttpStatus())
            .body(ErrorResponseFactory.from(ErrorCode.BAD_REQUEST, fieldErrors));
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
            .status(ErrorCode.FORBIDDEN_ACCESS.getHttpStatus())
            .body(ErrorResponseFactory.from(ErrorCode.FORBIDDEN_ACCESS));
    }

    @ExceptionHandler(InvalidValueException.class)
    protected ResponseEntity<ErrorResponse> handleInvalidValue(InvalidValueException e) {
        return ResponseEntity
            .status(e.getErrorCode().getHttpStatusCode())
            .body(ErrorResponseFactory.from(e.getErrorCode(), e.getData()));
    }

    @ExceptionHandler(ResourceNotFoundException.class)
    protected ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException e) {
        return ResponseEntity
            .status(e.getErrorCode().getHttpStatusCode())
            .body(ErrorResponseFactory.from(e.getErrorCode(), e.getData()));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleServerException(Exception e) {
        if (!"prod".equalsIgnoreCase(activeProfile)) {
            log.error("Unhandled exception: {}", e.getMessage(), e);
        } else {
            log.error("Unhandled exception: {}", e.getMessage());
        }
        return ResponseEntity
            .status(ErrorCode.SERVER_ERROR.getHttpStatus())
            .body(ErrorResponseFactory.from(ErrorCode.SERVER_ERROR, e.getClass().getSimpleName()));
    }

}
