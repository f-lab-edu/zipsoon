package com.zipsoon.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    // 인증, 인가
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "AUTH_001", "인증되지 않은 요청입니다"),
    INVALID_CREDENTIALS(HttpStatus.UNAUTHORIZED, "AUTH_002", "잘못된 인증 정보입니다"),
    INVALID_PASSWORD(HttpStatus.UNAUTHORIZED, "AUTH_003", "잘못된 비밀번호입니다"),

    // 사용자
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_001", "사용자를 찾을 수 없습니다"),
    USER_DUPLICATE(HttpStatus.CONFLICT, "USER_002", "이미 존재하는 사용자입니다"),

    // 매물
    PROPERTY_NOT_FOUND(HttpStatus.NOT_FOUND, "PROP_001", "매물을 찾을 수 없습니다"),

    // batch
    EXTERNAL_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "API_001", "외부 API 호출 실패"),
    RESOURCE_CONFLICT(HttpStatus.CONFLICT, "RESOURCE_001", "데이터 무결성 위반으로 저장에 실패했습니다"),

    // 요청
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "400_001", "잘못된 요청입니다"),
    FORBIDDEN_ACCESS(HttpStatus.FORBIDDEN, "403_001", "접근 권한이 없습니다"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500_001", "내부 서버 오류"),
    SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "500_002", "서버에서 오류가 발생했습니다");



    private final HttpStatus httpStatus;
    private final String code;
    private final String message;
    
    public int getHttpStatusCode() {
        return httpStatus.value();
    }
}
