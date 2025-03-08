package com.zipsoon.batch.infrastructure.repository.score;

import com.zipsoon.batch.domain.source.Park;
import com.zipsoon.batch.infrastructure.mapper.score.ParkScoreMapper;
import lombok.RequiredArgsConstructor;
import org.locationtech.jts.geom.Point;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class ParkScoreRepository {
    private final ParkScoreMapper parkScoreMapper;

    public List<Park> findParksWithin(Point location, double radiusMeters) {
        return parkScoreMapper.selectParksWithin(location, radiusMeters);
    }
}