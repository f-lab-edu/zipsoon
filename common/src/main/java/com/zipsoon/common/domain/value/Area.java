package com.zipsoon.common.domain.value;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * 면적을 나타내는 값 객체
 */
public record Area(BigDecimal squareMeters) {
    private static final BigDecimal PYEONG_CONVERSION_FACTOR = new BigDecimal("0.3025");
    private static final int SCALE = 2;
    private static final BigDecimal DEFAULT_VALUE = new BigDecimal("0.01");
    
    public Area {
        // 생성자에서 기본값 처리 (null이나 0 이하면 기본값 사용)
        if (squareMeters == null || squareMeters.compareTo(BigDecimal.ZERO) <= 0) {
            squareMeters = DEFAULT_VALUE;
        }
    }
    
    /**
     * 평으로 변환
     * 
     * @return 평 단위 면적
     */
    public BigDecimal toPyeong() {
        return squareMeters.multiply(PYEONG_CONVERSION_FACTOR)
                        .setScale(SCALE, RoundingMode.HALF_UP);
    }
    
    /**
     * 제곱미터 단위로 면적 객체 생성
     * 
     * @param squareMeters 제곱미터 면적
     * @return Area 객체
     */
    public static Area ofSquareMeters(BigDecimal squareMeters) {
        return new Area(squareMeters); // 생성자에서 null/0 체크함
    }
    
    /**
     * 제곱미터 단위로 면적 객체 생성 (double 값)
     */
    public static Area ofSquareMeters(double squareMeters) {
        return new Area(BigDecimal.valueOf(squareMeters)); // 생성자에서 0 체크함
    }
    
    /**
     * 평 단위로 면적 객체 생성
     * 
     * @param pyeong 평 면적
     * @return Area 객체
     */
    public static Area ofPyeong(BigDecimal pyeong) {
        if (pyeong == null || pyeong.compareTo(BigDecimal.ZERO) <= 0) {
            return new Area(null); // 생성자에서 기본값 사용
        }
        
        BigDecimal sqMeters = pyeong.divide(PYEONG_CONVERSION_FACTOR, SCALE + 2, RoundingMode.HALF_UP)
                                 .setScale(SCALE, RoundingMode.HALF_UP);
        return new Area(sqMeters);
    }
    
    /**
     * 평 단위로 면적 객체 생성 (double 값)
     */
    public static Area ofPyeong(double pyeong) {
        if (pyeong <= 0) {
            return new Area(null); // 생성자에서 기본값 사용
        }
        return ofPyeong(BigDecimal.valueOf(pyeong));
    }
    
    /**
     * 기본 면적 (0.01㎡) 반환
     */
    public static Area defaultArea() {
        return new Area(null); // 생성자에서 기본값 사용
    }
}
}