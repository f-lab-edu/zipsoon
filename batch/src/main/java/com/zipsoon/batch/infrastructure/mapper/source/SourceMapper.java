package com.zipsoon.batch.infrastructure.mapper.source;

import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface SourceMapper {
    @Update("${sql}")
    void executeDDL(@Param("sql") String sql);

    @Update("ALTER TABLE ${tableName} ADD COLUMN IF NOT EXISTS location geometry(Point, 4326)")
    void addLocationColumn(@Param("tableName") String tableName);

    @Update("UPDATE ${tableName} SET location = ST_SetSRID(ST_Point(경도, 위도), 4326) WHERE 위도 IS NOT NULL AND 경도 IS NOT NULL")
    int updateLocationCoordinates(@Param("tableName") String tableName);

    @Update("TRUNCATE TABLE ${tableName}")
    void truncateTable(@Param("tableName") String tableName);
}
