package com.zipsoon.common.domain.value;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * 가격을 나타내는 값 객체
 */
public record Price(BigDecimal amount) {
    public Price {
        Objects.requireNonNull(amount, "가격은 null일 수 없습니다");
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("가격은 음수일 수 없습니다");
        }
    }
    
    /**
     * 가격 생성
     * 
     * @param amount 금액
     * @return Price 객체
     */
    public static Price of(BigDecimal amount) {
        return new Price(amount);
    }
    
    /**
     * 0원 가격 반환
     */
    public static Price zero() {
        return new Price(BigDecimal.ZERO);
    }
}