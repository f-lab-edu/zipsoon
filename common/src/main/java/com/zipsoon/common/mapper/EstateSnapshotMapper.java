package com.zipsoon.common.mapper;

import com.zipsoon.common.domain.EstateSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Mapper
public interface EstateSnapshotMapper {
    default int getWgs84Srid() {
        return 4326;        // WGS84 좌표계 SRID 값
    }

    void insertEstateSnapshots(List<EstateSnapshot> estateSnapshots);
    List<EstateSnapshot> selectAllEstateSnapshots();
    List<EstateSnapshot> findEstatesInRadius(
        @Param("point") Point point,
        @Param("radius") int radius
    );

}