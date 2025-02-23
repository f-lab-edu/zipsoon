package com.zipsoon.api.exception.handler;

import com.zipsoon.api.exception.custom.ServiceException;
import com.zipsoon.api.exception.model.ErrorCode;
import com.zipsoon.api.exception.model.ErrorDetail;
import com.zipsoon.api.exception.model.ErrorResponse;
import com.zipsoon.api.exception.model.ErrorResponseFactory;
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

@Slf4j
@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalExceptionHandler {

    @Value("${spring.profiles.active:unknown}")
    private String activeProfile;

    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        return ResponseEntity
            .status(ErrorCode.BAD_REQUEST.getHttpStatus())
            .body(ErrorResponseFactory.from(
                ErrorCode.BAD_REQUEST,
                e.getBindingResult().getFieldErrors()
            ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    protected ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException e) {
        return ResponseEntity
            .status(ErrorCode.FORBIDDEN_ACCESS.getHttpStatus())
            .body(ErrorResponseFactory.from(ErrorCode.FORBIDDEN_ACCESS));
    }

    @ExceptionHandler(ServiceException.class)
    protected ResponseEntity<ErrorResponse> handleServiceException(ServiceException e) {
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
        if (!"prod".equalsIgnoreCase(activeProfile)) {
            log.error("Unhandled exception: {}", e.getMessage(), e);
        } else {
            log.error("Unhandled exception: {}", e.getMessage());
        }
        return ResponseEntity
            .status(ErrorCode.INTERNAL_ERROR.getHttpStatus())
            .body(ErrorResponseFactory.from(ErrorCode.INTERNAL_ERROR));
    }
}
