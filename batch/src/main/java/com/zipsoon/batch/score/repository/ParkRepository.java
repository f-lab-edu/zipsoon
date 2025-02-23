package com.zipsoon.batch.score.repository;

import com.zipsoon.batch.score.domain.Park;
import com.zipsoon.batch.score.mapper.ParkMapper;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ParkRepository {
    private final ParkMapper parkMapper;

    public List<Park> findParksWithin(Point location, double radiusMeters) {
        return parkMapper.findParksWithin(location, radiusMeters);
    }
}