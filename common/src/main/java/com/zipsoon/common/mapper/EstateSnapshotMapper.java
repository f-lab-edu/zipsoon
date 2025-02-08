package com.zipsoon.common.mapper;

import com.zipsoon.common.domain.EstateSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Mapper
public interface PropertySnapshotMapper {
    void insertPropertySnapshots(List<EstateSnapshot> estateSnapshots);
    List<EstateSnapshot> selectAllPropertySnapshots();
    List<EstateSnapshot> findPropertiesInRadius(
        @Param("point") Point point,
        @Param("radius") int radius
    );

}