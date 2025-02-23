package com.zipsoon.batch.score.mapper;

import com.zipsoon.batch.score.domain.Park;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Mapper
public interface ParkMapper {
    List<Park> findParksWithin(@Param("location") Point location, @Param("radius") double radiusMeters);
}