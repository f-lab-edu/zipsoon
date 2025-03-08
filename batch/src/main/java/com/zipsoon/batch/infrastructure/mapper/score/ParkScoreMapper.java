package com.zipsoon.batch.infrastructure.mapper.score;

import com.zipsoon.batch.domain.source.Park;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Mapper
public interface ParkScoreMapper {
    List<Park> selectParksWithin(@Param("location") Point location, @Param("radius") double radiusMeters);
}