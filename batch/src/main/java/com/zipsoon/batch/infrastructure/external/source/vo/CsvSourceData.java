package com.zipsoon.batch.infrastructure.external.source.vo;

/**
 * CSV 파일에서 로드된 소스 데이터 VO
 * 공원, 지하철역 등의 원본 데이터 형식을 표현
 */
public record CsvSourceData(
    String id,
    String name,
    String type,
    String latitude,
    String longitude,
    String metadata,
    String createdAt
) {
    /**
     * 위도를 double 형식으로 변환
     */
    public double getLatitude() {
        try {
            return Double.parseDouble(latitude);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }

    /**
     * 경도를 double 형식으로 변환
     */
    public double getLongitude() {
        try {
            return Double.parseDouble(longitude);
        } catch (NumberFormatException e) {
            return 0.0;
        }
    }
    
    /**
     * ID를 Long 형식으로 변환
     */
    public Long getIdAsLong() {
        try {
            return Long.parseLong(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
    /**
     * 유효한 위치 데이터를 가지고 있는지 확인
     */
    public boolean hasValidLocation() {
        return latitude != null && !latitude.isEmpty() &&
               longitude != null && !longitude.isEmpty() &&
               getLatitude() != 0.0 && getLongitude() != 0.0;
    }
}