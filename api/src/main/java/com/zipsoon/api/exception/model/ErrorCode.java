package com.zipsoon.api.exception.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
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
