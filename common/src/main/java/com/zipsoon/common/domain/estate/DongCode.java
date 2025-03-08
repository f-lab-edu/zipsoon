package com.zipsoon.common.domain.estate;

import java.io.Serial;
import java.io.Serializable;

/**
 * 법정동 코드 정보
 */
public record DongCode(String code, String name) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 새로운 동 코드를 생성합니다.
     *
     * @param code 동 코드
     * @param name 동 이름
     * @return 생성된 동 코드
     */
    public static DongCode of(String code, String name) {
        return new DongCode(code, name);
    }
}