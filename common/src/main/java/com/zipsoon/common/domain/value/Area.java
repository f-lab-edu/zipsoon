package com.zipsoon.common.domain.value;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * 면적을 나타내는 값 객체
 */
public record Area(BigDecimal squareMeters) {
    private static final BigDecimal PYEONG_CONVERSION_FACTOR = new BigDecimal("0.3025");
    
    public Area {
        Objects.requireNonNull(squareMeters, "면적은 null일 수 없습니다");
        if (squareMeters.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("면적은 양수여야 합니다");
        }
    }
    
    /**
     * 평으로 변환
     * 
     * @return 평 단위 면적
     */
    public BigDecimal toPyeong() {
        return squareMeters.multiply(PYEONG_CONVERSION_FACTOR)
                         .setScale(2, RoundingMode.HALF_UP);
    }
    
    /**
     * 제곱미터 단위로 면적 객체 생성
     * 
     * @param squareMeters 제곱미터 면적
     * @return Area 객체
     */
    public static Area ofSquareMeters(BigDecimal squareMeters) {
        return new Area(squareMeters);
    }
    
    /**
     * 평 단위로 면적 객체 생성
     * 
     * @param pyeong 평 면적
     * @return Area 객체
     */
    public static Area ofPyeong(BigDecimal pyeong) {
        BigDecimal squareMeters = pyeong.divide(PYEONG_CONVERSION_FACTOR, 2, RoundingMode.HALF_UP);
        return new Area(squareMeters);
    }
}