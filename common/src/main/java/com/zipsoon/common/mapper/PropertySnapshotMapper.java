package com.zipsoon.common.repository;

import com.zipsoon.common.domain.PropertySnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.locationtech.jts.geom.Point;

import java.util.List;

@Mapper
public interface PropertySnapshotMapper {
    void insertPropertySnapshots(List<PropertySnapshot> propertySnapshots);
    List<PropertySnapshot> selectAllPropertySnapshots();
    List<PropertySnapshot> findPropertiesInRadius(
        @Param("point") Point point,
        @Param("radius") int radius
    );

}