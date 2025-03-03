package com.zipsoon.batch.domain.source;

import com.zipsoon.batch.domain.estate.value.LocationValue;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;

/**
 * 점수 계산에 필요한 소스 데이터(공원, 지하철역 등)의 공통 도메인 모델
 */
@Getter
@ToString
@Builder
public class SourceData {
    /** 고유 식별자 */
    private final Long id;
    
    /** 소스 데이터 이름 */
    private final String name;
    
    /** 소스 데이터 유형 (PARK, SUBWAY 등) */
    private final String type;
    
    /** 위치 좌표 */
    private final LocationValue location;
    
    /** 추가 메타데이터 (JSON 형태) */
    private final String metadata;
    
    /** 생성 시간 */
    private final String createdAt;
    
    /**
     * 다른 위치와의 거리 계산 (미터 단위)
     * @param otherLocation 다른 위치
     * @return 거리 (미터)
     */
    public double distanceTo(LocationValue otherLocation) {
        return location.distanceToM(otherLocation);
    }
    
    /**
     * 주어진 거리 내에 있는지 확인
     * @param otherLocation 다른 위치
     * @param maxDistanceM 최대 거리 (미터)
     * @return 최대 거리 내에 있으면 true
     */
    public boolean isWithinDistance(LocationValue otherLocation, double maxDistanceM) {
        return distanceTo(otherLocation) <= maxDistanceM;
    }
}