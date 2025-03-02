package com.zipsoon.batch.estate.mapper;

import com.zipsoon.common.domain.Estate;
import com.zipsoon.common.domain.EstateSnapshot;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface EstateSnapshotMapper {
    default int getWgs84Srid() {
        return 4326;        // WGS84 좌표계 SRID 값
    }

    // 기존 메서드
    void insertEstateSnapshots(@Param("list") List<EstateSnapshot> estateSnapshots);
    List<EstateSnapshot> selectLatestAll();
    
    // 신규 메서드
    void insertEstates(@Param("list") List<Estate> estates);
    void migrateToSnapshot();
    List<Estate> selectAllEstates();
}