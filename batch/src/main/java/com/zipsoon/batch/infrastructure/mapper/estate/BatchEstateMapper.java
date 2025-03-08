package com.zipsoon.batch.infrastructure.mapper.estate;

import com.zipsoon.common.domain.Estate;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface BatchEstateMapper {
    default int getWgs84Srid() {
        return 4326;        // WGS84 좌표계 SRID 값
    }
    void insertAll(@Param("list") List<Estate> estates);
    void migrateToSnapshot();
    void truncateEstateTable();
    List<Estate> selectAll();
}