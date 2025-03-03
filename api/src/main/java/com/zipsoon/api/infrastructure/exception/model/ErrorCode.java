package com.zipsoon.api.infrastructure.exception.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증되지 않은 요청입니다"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_002", "유효하지 않은 토큰입니다"),
    MALFORMED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_003", "잘못된 형식의 토큰입니다"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_004", "만료된 토큰입니다"),
    UNSUPPORTED_TOKEN(HttpStatus.UNAUTHORIZED, "AUTH_005", "지원되지 않는 토큰입니다"),
    INVALID_SIGNATURE(HttpStatus.UNAUTHORIZED, "AUTH_006", "유효하지 않은 토큰 서명입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_007", "잘못된 인증 정보입니다"),
//    INSUFFICIENT_PERMISSIONS(HttpStatus.FORBIDDEN, "AUTH_008", "권한이 부족합니다"),

    // user
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다"),
    USER_DUPLICATE(HttpStatus.CONFLICT, "USER_002", "이미 존재하는 사용자입니다"),

    // api
    ESTATE_NOT_FOUND(HttpStatus.NOT_FOUND, "API_001", "매물을 찾을 수 없습니다"),

    // common request
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "400_001", "잘못된 요청입니다"),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "403_001", "접근 권한이 없습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500_001", "알 수 없는 오류가 발생했습니다");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    
    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
