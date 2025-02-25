package com.zipsoon.batch.score.calculator;

import com.zipsoon.batch.score.domain.Park;
import com.zipsoon.batch.normalize.normalizer.LinearScoreNormalizer;
import com.zipsoon.batch.normalize.normalizer.ScoreNormalizer;
import com.zipsoon.batch.score.repository.ParkRepository;
import com.zipsoon.common.domain.EstateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ParkScoreCalculator implements ScoreCalculator {
    private final ParkRepository parkRepository;
    private static final Long SCORE_TYPE_PRIMARY_KEY = 1L;
    private static final double WALKING_DISTANCE = 600.0; // 도보 10분

    @Override
    public Long getScoreId() {
        return SCORE_TYPE_PRIMARY_KEY;
    }

    @Override
    public ScoreNormalizer getNormalizer() {
        return new LinearScoreNormalizer();
    }

    @Override
    public double calculateRawScore(EstateSnapshot estate) {
        List<Park> nearbyParks = parkRepository.findParksWithin(
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

    private double calculateDistance(Point p1, Point p2) {
        final double EARTH_RADIUS = 6371000;

        double lat1 = Math.toRadians(p1.getY());
        double lon1 = Math.toRadians(p1.getX());
        double lat2 = Math.toRadians(p2.getY());
        double lon2 = Math.toRadians(p2.getX());
        double dLat = lat2 - lat1;
        double dLon = lon2 - lon1;

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                + Math.cos(lat1) * Math.cos(lat2)
                * Math.sin(dLon / 2) * Math.sin(dLon / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return EARTH_RADIUS * c;
    }
}