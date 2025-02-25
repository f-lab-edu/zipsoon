package com.zipsoon.batch;

import com.zipsoon.batch.score.calculator.ParkScoreCalculator;
import com.zipsoon.batch.score.domain.Park;
import com.zipsoon.batch.score.repository.ParkRepository;
import com.zipsoon.common.domain.EstateSnapshot;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ParkScoreCalculatorTest {
    private static final double ESTATE_LONGITUDE = 126.961692;
    private static final double ESTATE_LATITUDE = 37.571797;
    private static final double PARK_LONGITUDE = 126.9616546;
    private static final double PARK_LATITUDE = 37.572146;

    private ParkRepository parkRepository;
    private ParkScoreCalculator parkScoreCalculator;
    private GeometryFactory geometryFactory;

    @BeforeEach
    void setUp() {
        parkRepository = mock(ParkRepository.class);
        geometryFactory = new GeometryFactory();
        parkScoreCalculator = new ParkScoreCalculator(parkRepository);
    }

    @Test
    @DisplayName("주변에 공원이 없을 때는 0점을 반환한다")
    void shouldReturnZeroScoreWhenNoParkNearby() {
        // given
        EstateSnapshot estate = createEstateSnapshot(ESTATE_LONGITUDE, ESTATE_LATITUDE);
        when(parkRepository.findParksWithin(any(Point.class), eq(300.0)))
            .thenReturn(List.of());

        // when
        double score = parkScoreCalculator.calculateRawScore(estate);

        // then
        assertThat(score).isZero();
    }

    @Test
    @DisplayName("주변에 여러 공원이 있을 때는 양수의 점수를 반환한다")
    void shouldCalculatePositiveScoreWithMultipleParks() {
        // given
        EstateSnapshot estate = createEstateSnapshot(ESTATE_LONGITUDE, ESTATE_LATITUDE);

        List<Park> nearbyParks = List.of(
            createPark("park1", "공원1", PARK_LONGITUDE, PARK_LATITUDE, 1000.0),
            createPark("park2", "공원2", PARK_LONGITUDE, PARK_LATITUDE, 2000.0)
        );

        when(parkRepository.findParksWithin(any(Point.class), eq(600.0)))
            .thenReturn(nearbyParks);

        // when
        double score = parkScoreCalculator.calculateRawScore(estate);

        // then
        assertThat(score)
            .isGreaterThan(0.0)
            .isLessThanOrEqualTo(10.0);
    }

    private EstateSnapshot createEstateSnapshot(double longitude, double latitude) {
        return EstateSnapshot.builder()
            .location(geometryFactory.createPoint(new Coordinate(longitude, latitude)))
            .build();
    }

    private Park createPark(String id, String name, double longitude, double latitude, double area) {
        return Park.builder()
            .id(id)
            .name(name)
            .location(geometryFactory.createPoint(new Coordinate(longitude, latitude)))
            .area(area)
            .build();
    }
}