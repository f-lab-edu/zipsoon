package com.zipsoon.batch.score.calculator;

import com.zipsoon.batch.score.domain.Park;
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
    private static final double RADIUS = 300.0;     // 도보 5분 내 접근 가능한 거리
    private static final double MAX_SCORE = 10.0;

    @Override
    public String getScoreTypeName() {
        return "공원";
    }

    @Override
    public double calculate(EstateSnapshot estate) {
        List<Park> nearbyParks = parkRepository.findParksWithin(
            (Point) estate.getLocation(),
            RADIUS
        );
        log.debug("estate: {}({}), nearby parks: {}", estate.getEstateName(), estate.getLocation(), nearbyParks.size());

        if (nearbyParks.isEmpty()) {
            return 0.0;
        }

        // 1. 가장 큰 공원의 면적 점수: 6점 만점
        double largestParkArea = nearbyParks.stream()
            .mapToDouble(Park::getArea)
            .max()
            .orElse(0.0);
        double areaScore = Math.min(6.0, largestParkArea / 1000.0);

        // 2. 공원 개수에 따른 추가 점수: 4점 만점
        int parkCount = nearbyParks.size();
        double countScore = Math.min(4.0, parkCount * 1.5);

        double totalScore = Math.min(MAX_SCORE, areaScore + countScore);
        log.debug("area score: {}, count score: {}, total score: {}", areaScore, countScore, totalScore);
        return totalScore;
    }

}