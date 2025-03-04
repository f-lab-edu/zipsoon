package com.zipsoon.batch.infrastructure.repository.source;

import com.zipsoon.batch.infrastructure.mapper.source.SourceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class SourceRepository {
    private final SourceMapper sourceMapper;

    public void executeDDL(String sql) {
        sourceMapper.executeDDL(sql);
    }

    public void addLocationColumn(String tableName) {
        sourceMapper.addLocationColumn(tableName);
    }

    public int updateLocationCoordinates(String tableName) {
        return sourceMapper.updateLocationCoordinates(tableName);
    }

    public void dropTable(String tableName) {
        sourceMapper.dropTable(tableName);
    }
}