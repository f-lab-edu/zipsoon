package com.zipsoon.batch.domain.estate.value;

import java.util.Objects;

/**
 * 위치 정보를 표현하는 값 객체
 * 위도와 경도 정보를 포함하며, 두 위치 간 거리 계산 기능 제공
 */
public record LocationValue(double latitude, double longitude) {
    private static final double EARTH_RADIUS_KM = 6371.0;
    
    public LocationValue {
        // 유효한 위경도 범위 검증
        if (latitude < -90.0 || latitude > 90.0) {
            throw new IllegalArgumentException("Latitude must be between -90 and 90 degrees");
        }
        if (longitude < -180.0 || longitude > 180.0) {
            throw new IllegalArgumentException("Longitude must be between -180 and 180 degrees");
        }
    }
    
    /**
     * 두 위치 간 거리 계산 (Haversine 공식 사용)
     * @param other 비교할 다른 위치
     * @return 두 위치 간 거리 (km)
     */
    public double distanceToKm(LocationValue other) {
        Objects.requireNonNull(other, "Other location cannot be null");
        
        double lat1Rad = Math.toRadians(this.latitude);
        double lat2Rad = Math.toRadians(other.latitude);
        double lon1Rad = Math.toRadians(this.longitude);
        double lon2Rad = Math.toRadians(other.longitude);
        
        double latDiff = lat2Rad - lat1Rad;
        double lonDiff = lon2Rad - lon1Rad;
        
        double a = Math.sin(latDiff / 2) * Math.sin(latDiff / 2) +
                Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                Math.sin(lonDiff / 2) * Math.sin(lonDiff / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return EARTH_RADIUS_KM * c;
    }
    
    /**
     * 두 위치 간 직선 거리 계산 (미터 단위, 빠른 근사값)
     * @param other 비교할 다른 위치
     * @return 두 위치 간 대략적인 거리 (m)
     */
    public double distanceToM(LocationValue other) {
        return distanceToKm(other) * 1000.0;
    }
    
    @Override
    public String toString() {
        return String.format("(%.6f, %.6f)", latitude, longitude);
    }
}