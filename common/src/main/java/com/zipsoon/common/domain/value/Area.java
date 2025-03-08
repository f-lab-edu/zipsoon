package com.zipsoon.common.domain.value;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 면적을 나타내는 값 객체
 */
public record Area(double squareMeters) {
    private static final double PYEONG_CONVERSION_FACTOR = 0.3025;
    
    public Area {
        if (squareMeters <= 0) {
            throw new IllegalArgumentException("면적은 양수여야 합니다");
        }
    }
    
    /**
     * 평으로 변환
     * 
     * @return 평 단위 면적
     */
    public double toPyeong() {
        return Math.round(squareMeters * PYEONG_CONVERSION_FACTOR * 100) / 100.0;
    }
    
    /**
     * 제곱미터 단위로 면적 객체 생성
     * 
     * @param squareMeters 제곱미터 면적
     * @return Area 객체
     */
    public static Area ofSquareMeters(double squareMeters) {
        return new Area(squareMeters);
    }
    
    /**
     * BigDecimal에서 면적 객체 생성
     */
    public static Area ofSquareMeters(BigDecimal squareMeters) {
        if (squareMeters == null) {
            throw new IllegalArgumentException("면적은 null일 수 없습니다");
        }
        return new Area(squareMeters.doubleValue());
    }
    
    /**
     * 평 단위로 면적 객체 생성
     * 
     * @param pyeong 평 면적
     * @return Area 객체
     */
    public static Area ofPyeong(double pyeong) {
        return new Area(pyeong / PYEONG_CONVERSION_FACTOR);
    }
    
    /**
     * BigDecimal 평에서 면적 객체 생성
     */
    public static Area ofPyeong(BigDecimal pyeong) {
        if (pyeong == null) {
            throw new IllegalArgumentException("면적은 null일 수 없습니다");
        }
        return ofPyeong(pyeong.doubleValue());
    }
}