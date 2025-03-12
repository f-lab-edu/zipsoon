package com.zipsoon.batch.infrastructure.processor.score.calculator;

import com.zipsoon.batch.application.service.score.calculator.ScoreCalculator;
import com.zipsoon.batch.domain.source.Park;
import com.zipsoon.batch.infrastructure.processor.normalize.normalizer.LinearScoreNormalizer;
import com.zipsoon.batch.application.service.normalize.normalizer.ScoreNormalizer;
import com.zipsoon.batch.infrastructure.repository.score.ParkScoreRepository;
import com.zipsoon.common.domain.Estate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 공원 접근성 점수 계산기
 * 
 * <p>매물 주변 공원 정보를 기반으로 공원 접근성 점수를 계산합니다.
 * 점수는 다음 두 가지 요소를 고려하여 계산됩니다:</p>
 * <ul>
 *   <li>가장 가까운 공원까지의 거리 (40%): 도보 10분(600m) 이내 공원까지의 거리가 가까울수록 높은 점수</li>
 *   <li>주변 공원 총 면적 (60%): 도보 10분 이내 공원들의 총 면적이 클수록 높은 점수</li>
 * </ul>
 * 
 * <p>최종 원시 점수는 0~10 사이의 값으로, 이후 정규화 단계를 거칩니다.</p>
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkScoreCalculator implements ScoreCalculator {
    private final ParkScoreRepository parkScoreRepository;
    private static final Long SCORE_TYPE_PRIMARY_KEY = 1L;
    private static final double WALKING_DISTANCE = 600.0; // 도보 10분

    /**
     * 공원 점수 유형 ID 반환
     * @return 공원 점수 유형 ID (1)
     */
    @Override
    public Long getScoreId() {
        return SCORE_TYPE_PRIMARY_KEY;
    }

    /**
     * 이 점수 유형에 사용할 정규화 방식 반환
     * @return 선형 정규화 방식 (Linear Normalizer)
     */
    @Override
    public ScoreNormalizer getNormalizer() {
        return new LinearScoreNormalizer();
    }

    /**
     * 매물의 공원 접근성 원시 점수를 계산
     * 
     * @param estate 점수를 계산할 매물 객체
     * @return 계산된 원시 점수 (0~10 사이 값)
     */
    @Override
    public double calculateRawScore(Estate estate) {
        List<Park> nearbyParks = parkScoreRepository.findParksWithin(
            (Point) estate.getLocation(),
            WALKING_DISTANCE
        );

        if (nearbyParks.isEmpty()) {
            log.debug("estate {}({}):: no parks found =====> totalScore: 0", estate.getEstateName(), estate.getLocation());
            return 0;
        }

        // 1. 가장 가까운 공원까지의 거리 점수 (40%)
        double nearestDistance = nearbyParks.stream()
            .mapToDouble(park -> calculateDistance((Point) estate.getLocation(), park.getLocation()))
            .min()
            .orElse(WALKING_DISTANCE);
        double distanceScore = Math.max(0, (1 - nearestDistance / WALKING_DISTANCE) * 4);

        // 2. 총 면적 점수 (60%)
        double totalArea = nearbyParks.stream()
            .mapToDouble(Park::getArea)
            .sum();
        double areaScore = Math.min(totalArea / 50000.0, 1.0) * 6;

        double totalScore = distanceScore + areaScore;
        log.debug("estate {}({}):: distance: {}, area: {} =====> totalScore: {}",
                estate.getEstateName(), estate.getLocation(), distanceScore, areaScore, totalScore);
        return totalScore;
    }

    /**
     * 두 지점 간의 거리를 Haversine 공식을 사용하여 계산
     * 
     * @param p1 첫 번째 지점 (WGS84 좌표계)
     * @param p2 두 번째 지점 (WGS84 좌표계)
     * @return 두 지점 간의 거리 (미터 단위)
     */
    private double calculateDistance(Point p1, Point p2) {
        final double EARTH_RADIUS = 6371000; // 지구 반경 (미터)

        double lat1 = Math.toRadians(p1.getY());
        double lon1 = Math.toRadians(p1.getX());
        double lat2 = Math.toRadians(p2.getY());
        double lon2 = Math.toRadians(p2.getX());
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        // Haversine 공식
        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}