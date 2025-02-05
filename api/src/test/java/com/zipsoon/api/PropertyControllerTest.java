package com.zipsoon.api;

import com.zipsoon.common.config.TestDatabaseConfig;
import com.zipsoon.common.domain.PropertySnapshot;
import com.zipsoon.common.repository.PropertySnapshotMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
@Import(TestDatabaseConfig.class)
class PropertyMapperTest {

    @Autowired
    private PropertySnapshotMapper psm;

    private final GeometryFactory geometryFactory = new GeometryFactory();

    @Test
    @Sql("/datasets/ShouldFindPropertiesWithinDistance.sql")
    void shouldFindPropertiesWithinDistance() throws Exception {
        // Given
        Point origin = geometryFactory.createPoint(
            new org.locationtech.jts.geom.Coordinate(126.96, 37.572)    // 독립문역 사거리
        );
        int radius = 100;  // 미터

        // When
        List<PropertySnapshot> properties = psm.findPropertiesInRadius(origin, radius);

        // Then
        log.info("Properties within {}m of ({}, {}): total {} found", radius, origin.getX(), origin.getY(), properties.size());

        assertThat(properties)
            .hasSize(1)
            .extracting(PropertySnapshot::getPlatformId)
            .containsExactly("TEST1");
    }
}
