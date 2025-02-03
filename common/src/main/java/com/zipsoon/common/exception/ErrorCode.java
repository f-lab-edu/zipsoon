package com.zipsoon.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 인증
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH001", "Unauthorized or Invalid Token"),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "AUTH002", "Forbidden"),
    AUTH_INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH003", "Invalid email or password"),
    AUTH_EXPIRED_SESSION(HttpStatus.UNAUTHORIZED, "AUTH004", "Session expired"),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER001", "User not found"),
    USER_DUPLICATE(HttpStatus.BAD_REQUEST, "USER002", "User already exists"),
    USER_INVALID_OAUTH_PROVIDER(HttpStatus.BAD_REQUEST, "USER003", "Invalid OAuth provider"),

    // 요청
    REQUEST_INVALID(HttpStatus.BAD_REQUEST, "REQ001", "Invalid request parameters"),
    REQUEST_UNSUPPORTED_MEDIA_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "REQ002", "Unsupported media type"),
    REQUEST_METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "REQ003", "Method not allowed"),
    REQUEST_PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "REQ004", "Payload too large"),

    // 리소스
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "RES001", "Resource not found"),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "RES002", "Resource conflict"),

    // 서버
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SRV001", "Internal server error"),
    SERVER_SERVICE_UNAVAILABLE(HttpStatus.SERVICE_UNAVAILABLE, "SRV002", "Service temporarily unavailable"),

    // api
    REQUEST_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "API001", "요청 시간이 초과되었습니다"),
    TOO_MANY_REQUESTS(HttpStatus.TOO_MANY_REQUESTS, "API002", "요청이 너무 많습니다"),
    PAYLOAD_TOO_LARGE(HttpStatus.PAYLOAD_TOO_LARGE, "API003", "요청 크기가 너무 큽니다"),
    API_VERSION_NOT_SUPPORTED(HttpStatus.BAD_REQUEST, "API004", "지원하지 않는 API 버전입니다"),

    // batch
    BATCH_JOB_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "BATCH001", "배치 작업 실행에 실패했습니다"),
    EXTERNAL_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "BATCH002", "외부 API 호출에 실패했습니다"),
    CONCURRENT_BATCH_ERROR(HttpStatus.CONFLICT, "BATCH003", "동시 실행된 배치 작업이 있습니다"),
    INSUFFICIENT_RESOURCES(HttpStatus.SERVICE_UNAVAILABLE, "BATCH004", "서버 리소스가 부족합니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    
    public int getHttpStatus() {
        return httpStatus.value();
    }
}
