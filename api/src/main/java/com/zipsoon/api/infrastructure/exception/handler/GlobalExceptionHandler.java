package com.zipsoon.api.infrastructure.exception.handler;

import com.zipsoon.api.infrastructure.exception.custom.ServiceException;
import com.zipsoon.api.infrastructure.exception.model.ErrorCode;
import com.zipsoon.api.infrastructure.exception.model.ErrorDetail;
import com.zipsoon.api.infrastructure.exception.model.ErrorResponse;
import com.zipsoon.api.infrastructure.exception.model.ErrorResponseFactory;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn("[API:ERR] 유효하지 않은 요청 파라미터: {}", 
                e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining(", ")));
                
        return ResponseEntity
            .status(ErrorCode.BAD_REQUEST.getHttpStatus())
            .body(ErrorResponseFactory.from(
                ErrorCode.BAD_REQUEST,
                e.getBindingResult().getFieldErrors()
            ));
    }
    
    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorResponse> handleConstraintViolation(ConstraintViolationException e) {
        log.warn("[API:ERR] 제약조건 위반: {}", 
                e.getConstraintViolations().stream()
                .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                .collect(Collectors.joining(", ")));
                
        List<ErrorDetail> details = e.getConstraintViolations().stream()
            .map(violation -> new ErrorDetail(
                violation.getPropertyPath().toString(),
                violation.getMessage(),
                ErrorCode.CONSTRAINT_VIOLATION.getCode()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity
            .status(ErrorCode.CONSTRAINT_VIOLATION.getHttpStatus())
            .body(ErrorResponseFactory.from(
                ErrorCode.CONSTRAINT_VIOLATION,
                details,
                ErrorCode.CONSTRAINT_VIOLATION.getMessage()
            ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        log.warn("[API:ERR] 접근 권한 없음: {}", e.getMessage());
        
        return ResponseEntity
            .status(ErrorCode.FORBIDDEN_ACCESS.getHttpStatus())
            .body(ErrorResponseFactory.from(ErrorCode.FORBIDDEN_ACCESS));
    }

    @ExceptionHandler(ServiceException.class)
    protected ResponseEntity<ErrorResponse> handleServiceException(ServiceException e) {
        // Include error code, message, and details in log
        log.error("[API:ERR] 서비스 예외: {} - 코드: {} - 상세정보: {}", 
                  e.getMessage(), e.getErrorCode(), e.getDetails());
              
        List<ErrorDetail> details = e.getDetails().stream()
            .map(detail -> new ErrorDetail(
                detail.field(),
                detail.message(),
                detail.code()
            ))
            .toList();

        return ResponseEntity
            .status(e.getErrorCode().getHttpStatus())
            .body(ErrorResponseFactory.from(
                e.getErrorCode(),
                details,
                e.getMessage()
            ));
    }

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handleServerException(Exception e) {
        // Include stack trace in non-production environments
        if (!"prod".equalsIgnoreCase(activeProfile)) {
            log.error("[API:ERR] 처리되지 않은 예외", e);
        } else {
            log.error("[API:ERR] 처리되지 않은 예외: {}", e.getMessage());
        }
        return ResponseEntity
            .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
            .body(ErrorResponseFactory.from(ErrorCode.INTERNAL_ERROR));
    }
}
