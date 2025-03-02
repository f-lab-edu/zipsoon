package com.zipsoon.batch.score.mapper;

import com.zipsoon.common.domain.EstateScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ScoreMapper {
    void insertAll(@Param("list") List<EstateScore> scores);
    void migrateToScoreSnapshot();
}